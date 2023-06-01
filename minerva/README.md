# Minerva

This is an online help editor software.

## Configuration

**Environment variables**

- MINERVA_MIGRATION=1: migration enabled
- MINERVA_OLDURL==http://confluence-host/pages/viewpage.action?pageId=: Confluence URL begin, needed for migration
- MINERVA_USERFOLDER: set user folder for any user to static value, only for file-system mode
- MINERVA_KUNDE=1: customer version enabled (only 1 book allowed)
- LOGLEVEL: logging level (DEBUG, INFO, WARN, ERROR)

**AppConfig.properties**

- port: port number
- app.name: unique application name
- backend: 'gitlab' or 'file-system'
- workspaces: absolute folder for workspaces
- gitlab.url: http:// + host + post of Gitlab (no trailing slash)
- gitlab.project: Gitlab user name + '/' + Gitlab project name

## Build

    gradlew -Pbau=1 -Pdockerhub=host:port/ -Ptarget=base dockerBuildImage dockerPushImage
    gradlew -Pbau=1 -Pdockerhub=host:port/ -Ptarget=app  dockerBuildImage dockerPushImage

These commands build Docker images. You must specify host and port for your local Docker registry.
Start the Docker container with this command:

    docker run -d -p 8080:8080 -e TZ=Europe/Berlin -e LOGLEVEL=INFO \
        -v /home/user/minerva/AppConfig.properties:/AppConfig.properties \
        -v /home/user/minerva/data:/data \
        -e MINERVA_KUNDE=1 -e MINERVA_USERFOLDER=all-users \
        --name minerva host:port/minerva

## Login

In file-system mode you can use any login. In Gitlab mode you must use your Gitlab credentials.

