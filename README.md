# Trimness

![Travis CI Build Status](https://img.shields.io/travis/trimou/trimness/master.svg)](https://travis-ci.org/trimou/trimness)
![License](https://img.shields.io/badge/license-Apache%20License%202.0-yellow.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Trimness aims to be an extensible tool to build a **lightweight service for rendering templates** (e.g. a **simple reporting application**).
It's built on several open source projects.
The fundamental ones are [Trimou](http://trimou.org/) (mustache/handlebars-like templating engine), [Weld](http://weld.cdi-spec.org) (component model, extensibility) and [Vert.x](http://vertx.io) (web server, routing, event bus).

## Get started

Trimness is not in Maven central yet so you'll have to build it locally first:

```bash
$ git clone git@github.com:trimou/trimness.git
$ mvn clean install
```

The best place to start is probably the [simple example](https://github.com/trimou/trimness/tree/master/examples/simple).
To run an executable shaded/fat jar just type:

```bash
$ java -jar examples/simple/target/trimness-example-simple-shaded.jar
```

A simple UI with test form is located at `localhost:8080/test`.
Insert `commits-list.html` in template id field and hit render button.
You should get a link to a result - HTML page with list of last commits from this repository.

## How does it work?

Trimness's business is to render templates.
There are two ways to send a "render request".

1. HTTP endpoints
2. Vert.x event bus

### HTTP endpoints

| HTTP method | Path          | Consumes | Description |
|------------|---------------|--------------|--------------|
| POST | /render | application/json | Send sync/async render request |
| GET | /result/{resultId} | - | Get the result of a render request |
| DELETE | /result/{resultId} | - | Remove the result of a render request |
| GET | /result/link/{linkId} | - | Get the result for the specified link |
| GET | /monitor/health | - | Simple health-check resource |
| GET | /template/{id} | - | Attempt to find the template with the given id |

#### HTTP endpoints basics

Let's use `curl` to perform a very simple render request:

```bash
curl -X POST -H "Content-Type: application/json" -d '{ "templateContent" : "Hello {{model.name}}!", "model" : { "name" : "Foo"} }' http://localhost:8080/render
```

The reply should be `Hello Foo!`.
Let's analyze the request payload.
`content` property is used to specify the template for one-off rendering (TIP: it's much better to leverage the template providers and template cache - see below).
`model` property holds the data used during rendering (TIP: model is not the only source of data - see model providers below).

##### Asynchronous render requests

By default, the request is synchronous which means that the client is waiting for the rendered output.
If we change the payload to:

```json
{ "templateContent" : "Hello {{model.name}}!", "model" : { "name" : "Foo"}, "async": true }
```

Then trimness replies immediately with something like:

```json
{ "time" : "2017-05-19T11:25:50.393", "resultId" : "1495185748798", "timeout" : "2017-05-19T11:30:50.393"}
```

`time` is the current server time.
`timeout` is the time after which the result might be removed automatically (it depends on the underlying result repository implementation).
The `resultId` is used to pick up the result later:

```bash
curl http://localhost:8080/result/1495185748798
```

And the reply should be again `Hello Foo!`.

##### Result links

Sometimes it might be useful to specify a more memorable link that could be used instead of the result id:

```json
{ "templateContent" : "Hello {{model.name}}!", "model" : { "name" : "Foo"}, "async": true, "linkId" : "foo-1" }
```
`linkId` is used to specify a link that could be also used to get the result:

```bash
curl http://localhost:8080/result/link/foo-1
```

##### Result metadata

We can also specify the result type:

```bash
curl http://localhost:8080/result/1495185748798?resultType=metadata
```

In this case, the reply would be:

```json
{ "time" : "2017-05-19T11:25:50.393", "result" : { "id" : "1495185748798", "templateId" : "oneoff_1495201157642", "output" : "Hello Foo!", "status" : "SUCCESS" }}
```

### Vert.x Event Bus

A Vert.x message consumer is automatically registered for address `org.trimou.trimness.render`.
The consumer replies to the message with the result of rendering request.
The message payload should be the same as for the `/render` HTTP endpoint.

### Template providers

TODO

### Model providers

TODO

### Result repository

TODO

### Configuration

TODO

### Packaging and Deployment

TODO
