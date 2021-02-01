# Resident Webapp

Dev
===
* Copy `dev.template.conf` to `dev.conf` under `conf`
* Set `stream-name` to your kinesis instance

Run
===
`sbt -Dconfig.file=conf/dev.conf run`

Endpoint Access
===
`http://localhost:9000/{eventType}/average/{from-timestamp}/{to-timestamp}`