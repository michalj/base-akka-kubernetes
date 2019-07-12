About
=====

This is a minimal Akka app, with akka-discovery configured to work
with Kubernetes API. It's mostly based on Kubernetes API integration
test from <https://github.com/akka/akka-management.git>, but turned into
a standalone project.

Pre-requisites
==============

Make sure docker environment is configured:

    docker ps

and that you have access to a Kubernetes cluster:

    kubectl get nodes

Building
========

Change this line in `build.sbt` to whatever your docker repository is:

    dockerRepository := Some("localhost:30001")

You should now be able to build and publish:

    sbt docker:publish

Running
=======

Enable monitoring:

    apply -f monitoring.yaml
    
This assumes that you've installed prometheus operator with Helm, using
release name `monitoring`. Change `release: monitoring` to match your
set-up.
