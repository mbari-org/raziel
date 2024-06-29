# Configuration

Raziel is a configuration server, the keeper of secrets. It can be used to lookup the configuration of a M3/VARS installation. This requires that Raziel is configured with enough information to access the M3/VARS installation. The following is a list of configuration options:

## Environment Variables

The following environment variables are used to configure Raziel:

### [Raziel](https://github.com/mbari-org/raziel)

- `RAZIEL_HTTP_CONTEXT` - The context path for the HTTP server. Default is `/`.
- `RAZIEL_HTTP_PORT` - The port for the HTTP server. Default is `8080`.
- `RAZIEL_JWT_EXPIRATION` - The expiration time for the JWT token. Default is `180 days`.
- `RAZIEL_JWT_ISSUER` - The issuer for the JWT token. Default is `http://www.mbari.org`.
- `RAZIEL_JWT_SIGNING_SECRET` - The secret used to sign the JWT token. Default is `DEFAULT`.
- `RAZIEL_MASTER_KEY` - The master key used for access. Default is `DEFAULT`.


### [Annosaurus](https://github.com/mbari-org/annosaurus)

- `ANNOSAURUS_URL` - The URL for the Annosaurus service. Default is `http://localhost:8082/anno/v1`.
- `ANNOSAURUS_INTERNAL_URL` - The internal URL for the Annosaurus service. Default is `http://localhost:8082/anno/v1`. This is used for internal communication between services and may be different that the ANNOSAURUS_URL in a docker context.
- `ANNOSAURUS_TIMEOUT` - The timeout for the Annosaurus service. Default is `10 seconds`.
- `ANNOSAURUS_SECRET` - The secret for the Annosaurus service. Default is `secret`.

### [Beholder](https://github.com/mbari-org/beholder)

- `BEHOLDER_URL` - The URL for the Beholder service. Default is `http://localhost:8088`.
- `BEHOLDER_INTERNAL_URL` - The internal URL for the Beholder service. Default is `http://localhost:8088`. This is used for internal communication between services and may be different that the BEHOLDER_URL in a docker context.
- `BEHOLDER_TIMEOUT` - The timeout for the Beholder service. Default is `10 seconds`.
- `BEHOLDER_SECRET` - The secret for the Beholder service. Default is `secret`.

### [Charybdis](https://github.com/mbari-org/charybdis)

- `CHARYBDIS_URL` - The URL for the Charybdis service. Default is `http://localhost:8086`.
- `CHARYBDIS_INTERNAL_URL` - The internal URL for the Charybdis service. Default is `http://localhost:8086`. This is used for internal communication between services and may be different that the CHARYBDIS_URL in a docker context.
- `CHARYBDIS_TIMEOUT` - The timeout for the Charybdis service. Default is `10 seconds`.

### [Oni](https://github.com/mbari-org/oni)

Note that Oni is a modern replacement for the legacy VARS KB and User services.  The VARS KB and User services are still supported for backwards compatibility. If Oni is used, the VARS KB and User services should not be configured.

- `ONI_URL` - The URL for the Oni service. No default is configured. Example:`http://localhost:8083`.
- `ONI_INTERNAL_URL` - The internal URL for the Oni service. Default is `http://localhost:8083`. This is used for internal communication between services and may be different that the ONI_URL in a docker context.
- `ONI_TIMEOUT` - The timeout for the Oni service. Default is `10 seconds`.
- `ONI_SECRET` - The secret for the Oni service. Default is `secret`.

### [Panoptes](https://github.com/mbari-org/panoptes)

- `PANOPTES_URL` - The URL for the Panoptes service. Default is `http://localhost:8085/panoptes/v1`.
- `PANOPTES_INTERNAL_URL` - The internal URL for the Panoptes service. Default is `http://localhost:8085/panoptes/v1`. This is used for internal communication between services and may be different that the PANOPTES_URL in a docker context.
- `PANOPTES_TIMEOUT` - The timeout for the Panoptes service. Default is `10 seconds`.
- `PANOPTES_SECRET` - The secret for the Panoptes service. Default is `secret`.

### [Vampire Squid](https://github.com/mbari-org/vampire-squid)

- `VAMPIRE_SQUID_URL` - The URL for the Vampire Squid service. Default is `http://localhost:8084/vam/v1`.
- `VAMPIRE_SQUID_INTERNAL_URL` - The internal URL for the Vampire Squid service. Default is `http://localhost:8084/vam/v1`. This is used for internal communication between services and may be different that the VAMPIRE_SQUID_URL in a docker context.
- `VAMPIRE_SQUID_TIMEOUT` - The timeout for the Vampire Squid service. Default is `10 seconds`.
- `VAMPIRE_SQUID_SECRET` - The secret for the Vampire Squid service. Default is `secret`.

### [VARS KB Server](https://github.com/mbari-org/vars-kb-server)

The VARS KB Server is a legacy service. It is still supported for backwards compatibility. If Oni is used, the VARS KB and User services should not be configured.

- `VARS_KB_SERVER_URL` - The URL for the VARS KB server. No default is configured. Example: `http://localhost:8083/kb/v1`.
- `VARS_KB_SERVER_INTERNAL_URL` - The internal URL for the VARS KB server. Default is `http://localhost:8083/kb/v1`. This is used for internal communication between services and may be different that the VARS_KB_SERVER_URL in a docker context.
- `VARS_KB_SERVER_TIMEOUT` - The timeout for the VARS KB server. Default is `10 seconds`.
- `VARS_KB_SERVER_SECRET` - The secret for the VARS KB server. Default is `secret`.

### [VARS User Server](https://github.com/mbari-org/vars-user-server)

The VARS User Server is a legacy service. It is still supported for backwards compatibility. If Oni is used, the VARS KB and User services should not be configured.

- `VARS_USER_SERVER_URL` - The URL for the VARS User server. No default is configured. Example `http://localhost:8087/users/v1`.
- `VARS_USER_SERVER_INTERNAL_URL` - The internal URL for the VARS User server. Default is `http://localhost:8087/users/v1`. This is used for internal communication between services and may be different that the VARS_USER_SERVER_URL in a docker context.
- `VARS_USER_SERVER_TIMEOUT` - The timeout for the VARS User server. Default is `10 seconds`.
- `VARS_USER_SERVER_SECRET` - The secret for the VARS User server