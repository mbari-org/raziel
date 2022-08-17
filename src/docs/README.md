# raziel

Raziel is a configuration server for the Video Annotation and Reference System (VARS). It provides health checks for all the VARS services as well as access to security keys for the individual services.

## Overview

Raziel provides a security handshake with the following flow:

```mermaid
sequenceDiagram 
    actor U as User
    participant R as Raziel
    participant A as Annosaurus

    U->>+R: POST /config/auth<br/>Authorization: Basic <auth>
    R-->>-U: {"accessToken": <JWT 1> }
    U->>+R: GET /config/endpoints<br/>Authorization: Bearer <JWT 1>
    R-->>-U: JSON with secrets
    U->>+A: POST /auth<br/>Authorization: APIKEY <secret>
    A-->>-U: {"access_token": <JWT 2> ... }
    U->>+A: POST/PUT/DELETE /some/method<br/>Authorization: Bearer <JWT 2>
    A-->>-U: 200 w/ content

```

## Documentation

Documentation is at <https://mbari-org.github.io/raziel/>