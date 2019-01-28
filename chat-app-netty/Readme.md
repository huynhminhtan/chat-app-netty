## Refer:

- protopuf with reactjs / nodejs: https://webapplog.com/json-is-not-cool-anymore/

- build `repeated` property protopuf java:

  - https://stackoverflow.com/questions/29170183/how-to-set-repeated-fields-in-protobuf-before-building-the-message
  - https://stackoverflow.com/questions/32081493/how-to-set-google-protobuf-repeated-field-in-java


- netty-rest *** / CORS: https://github.com/yjmyzz/netty-rest


## API

### Login

```
POST/ http://localhost:6898/login

req
{
	"password": "1234567",
	"phone": "0999888771"
}

re / password not match
{"status":"password-not-match","content":null}

re / phone not exists
{"status":"phone-not-exists","content":null}

re / login success
{"status":"success","content":{"userName":"mtSiniChi","password":"1234567","phone":"0999888774"}}

```

### Register

```
POST/ http://localhost:6898/register

req
{
        userName:"mtSiniChi",
        password: "1234567",
        phone: "0999888773"
}

re / phone is exists
{"status":"isExists","content":null}

re / register sucess
{"status":"success","content":{"userName":"mtSiniChi","password":null,"phone":"0999888775"}}

```

### Load Friends

```
GET/ http://localhost:6898/loadFriends

re
[{"userName":"mtSiniChi","phone":"0999888771"},{"userName":"mtSiniChi","phone":"0999888775"}]

```