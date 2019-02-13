# coding=utf-8
from cassandra.cluster import Cluster
from cassandra.query import SimpleStatement
import random
import time
import sys
import signal
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("cluster_ip", help="The ip of one of the cluster nodes")
parser.add_argument("-m", "--mode", help="set the mode the script will run in",
                    choices=["READ", "INSERT", "RANDOM"], default="READ")
parser.add_argument("-p", "--pause", help="set the pause in seconds between each request", type=float, default=0.001)
parser.add_argument("-b", "--batch_size",
                    help="set the amount of lines to insert on each request has no effect in READ mode",
                    type=int, default=1)
parser.add_argument("-t", "--table", help='set the table name for INSERT mode. Otherwise a random table is selected')
parser.add_argument("-k", "--keyspace", help="set the keyspace name. Otherwise a random keyspace is selected")

args = parser.parse_args()
actions = 0
write_actions = 0
read_actions = 0
random_table = not args.table


def setup_session(cip, ksp):
    # cip - cluster ip
    # ksp - keyspace
    print("starting session")
    sess = Cluster([cip]).connect(ksp)
    print("Connected to cluster: " + sess.cluster.metadata.cluster_name)
    return sess


def select_random_table(sess, ksp):
    return random.choice(sess.cluster.metadata.keyspaces[ksp].tables.keys())


def select_random_keyspace(sess):
    keys = sess.cluster.metadata.keyspaces.keys()
    result = random.choice(keys)
    while result.startswith("system"):
        result = random.choice(keys)
    return result


# inserts amount of random rows into the table named tableName pausing between each insert for pauseInSeconds
def insert_random_rows(sess, table_name, current_rows, columns, batch_size):
    column_string = ",".join(columns)
    insert_line = " INSERT INTO " + table_name + " (" + column_string + ") VALUES (" + "%s," * (len(columns) - 1) + "%s);"

    if batch_size > 1:
        statement = "BEGIN BATCH "
        for i in range(batch_size):
            statement += insert_line
        statement += " APPLY BATCH"
        statement = SimpleStatement(statement)
    else:
        statement = SimpleStatement(insert_line)

    batch_values = create_random_values(batch_size, current_rows)
    sess.execute_async(statement, batch_values)


def remove_rows(sess, amount, table_name, pause_in_seconds):
    fetchStatement = SimpleStatement("SELECT * FROM " + table_name)
    deleteStatement = SimpleStatement("DELETE FROM " + table_name + " WHERE id=%s IF EXISTS")
    rows = sess.execute(fetchStatement)
    i = 1
    for row in rows:
        if i >= amount:
            return
        sess.execute_async(deleteStatement, [row.id])
        i += 1
        pause(pause_in_seconds)


def read_from_table(sess, table_name):
    sess.execute_async(SimpleStatement("SELECT * FROM %s LIMIT %d" % (table_name, random.choice(range(1, 200)))))
    return


def pause(time_in_seconds):
    if time_in_seconds > 0:
        time.sleep(time_in_seconds)


def create_random_values(batch_size, rows):
    result = []
    for i in range(batch_size):
        random_row = random.choice(rows)
        for col in random_row:
            result.append(col)
    return result


def random_string():
    legalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0987654321"
    result = ""
    for i in range(15):
        result += random.choice(legalChars)
    return result


def choose_new_table():
    if random_table and actions % 10000 == 0:
        args.table = select_random_table(session, args.keyspace)
        print("new table " + args.table + " was chosen")


def sigint_handler(signal, frame):
    print("\nperformed %d actions in %s mode\n write actions: %d\n read actions: %d" % (actions, args.mode, write_actions, read_actions))
    sys.exit(0)


signal.signal(signal.SIGINT, sigint_handler)

session = setup_session(args.cluster_ip, args.keyspace)
if not args.keyspace:
    args.keyspace = select_random_keyspace(session)
if random_table:
    args.table = select_random_table(session, args.keyspace)
session.set_keyspace(args.keyspace)
print("Selected table %s from keyspace %s" % (args.table, args.keyspace))
print("stressing database by sending {} queries every {} seconds...".format(args.mode, args.pause))

fetchStatement = SimpleStatement("SELECT * FROM " + args.table)
rows = session.execute(fetchStatement)
column_names = session.cluster.metadata.keyspaces[args.keyspace].tables[args.table].columns.keys()

if args.mode == "READ":
    while True:
        read_from_table(session, args.table)
        actions += 1
        pause(args.pause)
if args.mode == "INSERT":
    rows = session.execute(fetchStatement)
    while True:
        insert_random_rows(session, args.table, rows.current_rows, column_names, args.batch_size)
        actions += 1
        write_actions += 1
        pause(args.pause)
if args.mode == "RANDOM":
    while True:
        read_mode = random.choice([True, False])
        if read_mode:
            read_from_table(session, args.table)
            read_actions += 1
        else:
            insert_random_rows(session, args.table, rows.current_rows, column_names, args.batch_size)
            write_actions += 1
        actions += 1
        pause(args.pause)
