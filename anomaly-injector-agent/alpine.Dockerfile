FROM python:3.6 as build

ENV STRESS_VERSION=0.09.57
ENV CPULIMIT_VERSION=0.2

RUN apt-get update && apt-get install -y \
    make \
    gcc \
&& rm -rf /var/lib/apt/lists/*

# download stress-ng and cpulimit
WORKDIR /src
COPY anomalies/c_src c_src
RUN rm -fr stress-ng &&  rm -fr cpulimit
ADD https://github.com/ColinIanKing/stress-ng/archive/V${STRESS_VERSION}.tar.gz .
ADD https://github.com/opsengine/cpulimit/archive/v${CPULIMIT_VERSION}.tar.gz .

RUN tar -xf V${STRESS_VERSION}.tar.gz && mv stress-ng-${STRESS_VERSION} stress-ng && \
    tar -xf v${CPULIMIT_VERSION}.tar.gz && mv cpulimit-${CPULIMIT_VERSION} cpulimit

# make c projects
WORKDIR /src/stress-ng
RUN STATIC=1 make

WORKDIR /src/cpulimit
RUN STATIC=1 make

WORKDIR /src/c_src/disk_pollution
RUN STATIC=1 make

WORKDIR /src/c_src/fork_flooding
RUN STATIC=1 make

WORKDIR /src/c_src/mem_alloc
RUN STATIC=1 make

# build alpine image
FROM python:3.6-alpine

# install dependencies
RUN apk add --no-cache gcc \
    libc-dev \
    linux-headers \
    iproute2 \
    iptables

WORKDIR /usr/src/app
COPY . .
# copy binaries of build image
WORKDIR anomalies/binaries
COPY --from=build /src/stress-ng/stress-ng .
COPY --from=build /src/cpulimit/src/cpulimit .
COPY --from=build /src/c_src/disk_pollution/disk_pollution .
COPY --from=build /src/c_src/fork_flooding/fork_flooding .
COPY --from=build /src/c_src/mem_alloc/mem_alloc .
COPY --from=build /src/c_src/stress/stress .
# install pip dependencies
WORKDIR /usr/src/app
RUN pip install --no-cache-dir -r requirements.txt
ENTRYPOINT [ "python3.6", "./injector_agent.py" ]