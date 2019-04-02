Distributed Anoamly Injector
============================

Application for injecting system anomalies via web service requests.


Install instructions
--------------------

Most anomalies rely on target-specific compiled binary files.
After cloning, execute:
```shell
make.sh
```
It sets up the required binary files. 

Note that stress-ng and cpulimit are precompiled binaries, which may not work on your target system.
If that's the case, clone the following git repositories with
 ```shell
git clone https://github.com/ColinIanKing/stress-ng.git
git clone https://github.com/opsengine/cpulimit.git
```
navigate into the cloned directory and build with
```shell
make
```
The resulting binaries have to be copied into the ./anomalies/binaries directory.
