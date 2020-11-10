# topic-reloader

Ferramenta utilizada para recarregar eventos do S3 no Kafka usando Spark Batch, tendo como origem arquivos avro no S3 e como destino um tópico no Kafka.

Known issues.
- Os schemas precisam ser compativeis para que funcione.
- Os tópicos e schemas precisam ser criados e registrados previamente.
- Ainda nao foi testado em staging por conta do acesso do EMR, mas foi testado em dev com os dados de staging e funcionou.
- Ainda nao é possível filtrar quais eventos se deseja recarregar.
- É necessário registrar os passwords o Schema Registry e do Kafka na Parameter Store

###Stack
Foi utilizada a Avro Bridge for Spark para a conversao do dataframe spark para o avro https://github.com/AbsaOSS/ABRiS. Consultar: [How to use Abris with Confluent avro (with examples)](https://github.com/AbsaOSS/ABRiS/blob/master/documentation/confluent-avro-documentation.md)

###Utilizacao
1- Para cada topico a ser recarregado, deve-se criar um novo tópico com o sufixo `-reloaded`. 
Exemplo: Desejo recarregar o topico: `creditas.home.payments.payment_accepted_v1`, deve-se criar um novo topico `creditas.home.payments.payment_accepted_v1-reloaded`

2- Dar a permissão de escrita para a api-key que será usada no reprocessamento desse tópico.

3- Registrar o schema do tópico original no tópico `reloaded`.

4- Deve-se garantir que os arquivos a serem recarregados estao no diretorio cujo pattern condiz com o pattern apresentado no arquivo `resources/application.conf`

5- Compilar a aplicação usando o comando abaixo na raiz do projeto: 
```
sbt assembly
```

6- Para execução em desenvolvimento - criar o cluster usando `scripts/create-cluster-dev.sh`

7- Para execução em desenvolvimento - executar `scripts/run-job-dev.sh`
    Exemplo: 
```
aws-vault exec fintech-dev -- ./scripts/run-job-dev.sh --cluster-id=j-RVBQPD1L5NJT --jar=$(find . -name "*assembly*.jar") --topic-name=creditas.servicing.billing.installment_payment_delayed_v1
```

ps. o comando find na hora de passar o jar evita que se tenha que digitar o nome do arquivo.

