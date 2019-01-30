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

re / register success
{"status":"success","content":{"userName":"mtSiniChi","password":null,"phone":"0999888775"}}

```

### Load Friends

```
GET/ http://localhost:6898/loadFriends

re
[
   {
      "userName":"mtSiniChi",
      "phone":"0999888771"
   },
   {
      "userName":"Hoai Linh",
      "phone":"0999888775"
   }
]

```

### Load messages by conversationID

```

POST/ http://localhost:6898/loadMessagesByConversationID

req

{
	"conversationID": "1548761179"
}

re

[
   {
      "senderID":"0999899111112",
      "senderName":"mtSiniChi",
      "content": "Mot ngay hanh phuc som mai",
      "time":"1548763063",
      "conversationID":"1548761179"
   },
   {
      "senderID":"0223299111888",
      "senderName":"Unknown",
      "content":"Anh nang chua chan",
      "time":"1548762247",
      "conversationID":"1548761179"
   }
]

```

## Socket

### First request

```
req

{
	messageType: "firstRequest",
	content: {
		userID: "1119899332"
	}
}

```

### Create conversation

```
req

{
    messageType: "createConversation",
    content: {
        conversationName: "Class 12A",
        users: [
            "0999899882",
            "0999899112",
            "0999899332"
        ]
    }
}

re

{
   "messageType":"createConversation",
   "content":{
      "conversationID":"1548733330",
      "conversationName":"Class 12A"
   }
}

```

###  Load all conversations of current user

```
re

{
   "messageType":"loadAllConversationsForUser",
   "content":[
      {
         "conversationID":"1548747664",
         "conversationName":"Class 12A"
      },
      {
         "conversationID":"1548747714",
         "conversationName":"Trip fun"
      }
   ]
}

```

### Send message

```
req

{
    messageType: "sendMessage",
    content: {
        sender: "0999888771",
        content: "Hoan hoan trong nang",
        conversationID: "1548761179"
    }
}

re

{
   "messageType":"sendMessage",
   "content":{
      "senderID":"0999888771",
      "senderName":"mtSiniChi",
      "content":"Hoan hoan trong nang",
      "time":"1548821904",
      "conversationID":"1548761179"
   }
}

```