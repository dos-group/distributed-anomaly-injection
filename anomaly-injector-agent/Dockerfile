FROM python:3.6

WORKDIR /usr/src/app

COPY . .
RUN apt-get update && apt-get install -y \
    stress-ng cpulimit make iptables iproute iproute2
RUN pip install --no-cache-dir -r requirements.txt
RUN cd anomalies/c_src && rm -fr stress-ng && git clone https://github.com/ColinIanKing/stress-ng.git && \
    cd stress-ng && make && cd /usr/src/app
RUN cd anomalies/c_src && rm -fr cpulimit && git clone https://github.com/opsengine/cpulimit.git && \
    cd cpulimit && make && cd /usr/src/app
RUN ./make.sh

ENTRYPOINT [ "python3.6", "./injector_agent.py" ]
