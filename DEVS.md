Install Docker and use the following command to run a Jenkins server:

```
docker run \
    --name jenkins \
    -p 9090:8080 \
    -p 50000:50000 \
    -p 8000:8000 \
    -v /absolute/path/to/local/jenkins_home:/var/jenkins_home \
    --restart=on-failure \
    --env JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n \
    jenkins/jenkins:lts
```

This will start Jenkins on port 9090. In this repo, run `hpi:hpi` to generate an installable package to upload to Jenkins.