FROM alpine:latest

WORKDIR /discord

ADD *.jar /discord

RUN apk --update add openjdk8-jre

CMD /bin/ash -l -c "DISCORD_TOKEN=\"$(cat /run/secrets/stationeering_discord_token)\" AWS_SQS_URL=\"$(cat /run/secrets/stationeering_aws_sqs_url)\" AWS_ACCESS_KEY_ID=\"$(cat /run/secrets/stationeering_aws_access_key)\" AWS_SECRET_ACCESS_KEY=\"$(cat /run/secrets/stationeering_aws_secret_key)\" AWS_REGION=eu-west-1 java -jar /discord/discord*.jar"
