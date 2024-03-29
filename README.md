# raziel

![Unit Tests](https://github.com/mbari-org/raziel/actions/workflows/test.yml/badge.svg)

![MBARI logo](src/docs/_asssets/images/logo-mbari-3b.png)

Raziel is a configuration server, the keeper of secrets. It can be used to lookup the configuration of a M3/VARS installation.

The flow of a security handshake is:

```mermaid
sequenceDiagram 
    actor U as User
    participant R as Raziel
    participant A as Annosaurus

    rect rgb(150, 114, 114)
    U->>+R: POST /config/auth<br/>Authorization: Basic <auth>
    R-->>-U: {"accessToken": <JWT 1> }
    U->>+R: GET /config/endpoints<br/>Authorization: Bearer <JWT 1>
    R-->>-U: JSON with secrets
    end
    rect rgb(114, 150, 114)
    U->>+A: POST /auth<br/>Authorization: APIKEY <secret>
    A-->>-U: {"access_token": <JWT 2> ... }
    U->>+A: POST/PUT/DELETE /some/method<br/>Authorization: Bearer <JWT 2>
    A-->>-U: 200 w/ content
    end

```

## Documentation

Documentation is at <https://mbari-org.github.io/raziel/>

## Notes

Documentation can be added as markdown files in `docs` and will be included automatically when you run `laikaSite`.
