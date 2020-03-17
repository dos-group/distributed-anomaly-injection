# distributed-anomaly-injection

This repository implements an extensible, agent-based system anomaly injector.
Builds uppon several system anomaly simulation tools.
Most implemented anomalies focus on two aspects:
- Utilizing system resources in various ways
- Modifying the network connectivity of the host

The repository contains two main executables:
- *anomaly-injector-agent* implements a REST API for controlling anomalies remotely
- *anomaly-experiment-controller* executes a controlled, predefined experiment procedure on multiple hosts, using the anomaly injection REST API
