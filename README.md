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

Create role and role binding to allow the app to find fellow instances
via Kubernetes API:

    kubectl apply -f role.yaml
    # this assumes you run everything in namespace 'base-akka-dev'
    kubectl apply -f role-binding.yaml

Create the deployment and the service (mind that you might need to
change the `image` to match your repository name):

    kubectl apply -f deployment.yaml
    kubectl apply -f service.yaml

Enable monitoring:

    kubectl apply -f monitoring.yaml
    
This assumes that you've installed prometheus operator with Helm, using
release name `monitoring`. Change `release: monitoring` to match your
set-up.
