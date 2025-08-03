# Instrucciones para Claude

Crear un nuevo data source para obtener las ofertas del p2p.

Recuerda usar todas las clases y directorios disponibles en data.

Tienes el ApiConfig para añadir el /p2p/index y el HttpClientFactory


Esta es la doc oficial:

Consulta las ultimas operaciones disponibles en el P2P.

Se pueden pasar todos los parametros de filtrado y seleccion disponibles:

    type ['buy', 'sell']

    min

    max

    coin

    my (para motrar solo tus propias ofertas)

    vip (para mostrar las ofertas VIP)

A tener en cuenta que este endpoint es púbico con información limitada segun el tipo de usuario.

    Guest: Anuncios con 8 minutos de atraso.
    Usuario nonKYC con 5 minutos de atraso.
    Usuario KYC con 2 minutos de atraso.
    Usuario GOLD en tiempo real.
    Usuario VIP en tiempo real.



Como vez es un metodo GET y este es un ejemplo del subpath /p2p/index?type=buy\&coin=ETECSA\&min=1\&max=50  
Lo que se envia en ese caso son query param, nada en el body

Recuerda que debes usar el accessToken guardado en BD con getAccessToken para poder enviarlo en la peticion.

Te comparto algunos ejemplos del responde

{
"current_page": 1,
"data": [
{
"uuid": "1d8ad0fa-d7ad-43d9-8d67-4669321a45f6",
"user_id": 184594,
"type": "sell",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "100.00",
"receive": "16000.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T18:33:08.000000Z",
"updated_at": "2023-04-14T20:02:04.000000Z"
},
{
"uuid": "24958dbe-8dca-4692-8b4d-817c4f005a1b",
"user_id": 1,
"type": "sell",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "940.00",
"receive": "940.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T18:32:47.000000Z",
"updated_at": "2023-02-16T18:32:47.000000Z"
},
{
"uuid": "52977ca9-5e00-4a6f-a2a0-0b41db64e485",
"user_id": 1,
"type": "sell",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "1000.00",
"receive": "1000.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T18:32:18.000000Z",
"updated_at": "2023-02-16T18:32:18.000000Z"
},
{
"uuid": "3e8ab87a-80a7-4467-a3b6-ee09062cbdff",
"user_id": 1,
"type": "sell",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "1000.00",
"receive": "1000.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T18:31:47.000000Z",
"updated_at": "2023-02-16T18:31:47.000000Z"
},
{
"uuid": "85ac348e-e537-4271-8511-6bc3133b4c35",
"user_id": 187465,
"type": "sell",
"coin": "TRX",
"peer_id": 0,
"amount": "5.00",
"receive": "72.166582330105",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T18:06:12.000000Z",
"updated_at": "2023-02-16T18:06:12.000000Z"
},
{
"uuid": "41ff24ae-04ed-487f-9ff0-fc9043b452a0",
"user_id": 183925,
"type": "buy",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "90.00",
"receive": "14490.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T18:04:43.000000Z",
"updated_at": "2023-02-16T18:04:43.000000Z"
},
{
"uuid": "4909f58b-4011-4eb1-8e12-395c1b1d06be",
"user_id": 29069,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "100.00",
"receive": "99.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T18:02:01.000000Z",
"updated_at": "2023-02-16T18:02:01.000000Z"
},
{
"uuid": "26d80e4b-d27c-49d5-951b-f25d87586325",
"user_id": 53675,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "100.00",
"receive": "100.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T17:56:20.000000Z",
"updated_at": "2023-02-16T17:56:20.000000Z"
},
{
"uuid": "573b6b5a-b8f6-4972-9f7e-be6a84e4558f",
"user_id": 53675,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "100.00",
"receive": "99.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T17:46:07.000000Z",
"updated_at": "2023-02-16T17:46:07.000000Z"
},
{
"uuid": "da9b9ed6-d970-456d-b874-b0f2e88268e7",
"user_id": 25688,
"type": "sell",
"coin": "USDT",
"peer_id": 0,
"amount": "400.00",
"receive": "400.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T17:40:35.000000Z",
"updated_at": "2023-02-16T17:40:35.000000Z"
},
{
"uuid": "42e95773-f406-4a84-b533-3200df026cf7",
"user_id": 184969,
"type": "sell",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "6.00",
"receive": "6.300000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T17:39:32.000000Z",
"updated_at": "2023-02-16T17:39:32.000000Z"
},
{
"uuid": "1bc3386e-32cc-4a99-ad74-8502c742aaea",
"user_id": 533,
"type": "sell",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "13.00",
"receive": "2145.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T16:58:51.000000Z",
"updated_at": "2023-02-16T16:58:51.000000Z"
},
{
"uuid": "09320f7a-7822-4dc0-bb06-f07b21d182a4",
"user_id": 533,
"type": "sell",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "15.00",
"receive": "2475.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T16:50:56.000000Z",
"updated_at": "2023-02-16T16:50:56.000000Z"
},
{
"uuid": "601ed82b-4644-4659-bff4-af54dc2377b9",
"user_id": 1078,
"type": "sell",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "2.00",
"receive": "330.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T16:47:52.000000Z",
"updated_at": "2023-02-16T16:47:52.000000Z"
},
{
"uuid": "22b008b8-b6d7-4156-916a-9b015f4788b7",
"user_id": 9908,
"type": "sell",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "20.00",
"receive": "20.500000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T16:15:36.000000Z",
"updated_at": "2023-02-16T16:15:36.000000Z"
},
{
"uuid": "f6e82282-48b8-408c-91f1-8de76048319c",
"user_id": 184969,
"type": "sell",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "5.00",
"receive": "5.200000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T16:12:52.000000Z",
"updated_at": "2023-02-16T16:12:52.000000Z"
},
{
"uuid": "e833285e-fc9e-4b94-a138-279854767c4a",
"user_id": 5912,
"type": "sell",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "100.00",
"receive": "102.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T15:53:23.000000Z",
"updated_at": "2023-02-16T16:07:33.000000Z"
},
{
"uuid": "78e799be-7ead-4a0f-8ce4-8acaa8b5717c",
"user_id": 3822,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "100.00",
"receive": "100.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T15:45:19.000000Z",
"updated_at": "2023-02-16T15:45:19.000000Z"
},
{
"uuid": "97d9803c-1b10-4f52-aef5-0d437000d9ce",
"user_id": 762,
"type": "buy",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "50.00",
"receive": "8000.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T15:29:10.000000Z",
"updated_at": "2023-02-16T15:29:10.000000Z"
},
{
"uuid": "fd711c9b-423f-46c6-ab99-dba08cfaa87f",
"user_id": 184969,
"type": "sell",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "1.00",
"receive": "170.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T14:53:01.000000Z",
"updated_at": "2023-02-16T14:53:01.000000Z"
},
{
"uuid": "fc83b73f-b00a-4c51-bd41-bff372dfdb12",
"user_id": 5444,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "20.00",
"receive": "20.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T14:33:31.000000Z",
"updated_at": "2023-02-16T14:33:31.000000Z"
},
{
"uuid": "e11e351b-f16a-4d09-9db3-1620f1e304d9",
"user_id": 5444,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "50.00",
"receive": "50.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T14:32:01.000000Z",
"updated_at": "2023-02-16T14:32:01.000000Z"
},
{
"uuid": "5b17f700-34c8-4ce3-a8f1-d013cff851ed",
"user_id": 5444,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "100.00",
"receive": "100.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T14:31:16.000000Z",
"updated_at": "2023-02-16T14:31:16.000000Z"
},
{
"uuid": "81716d05-be4f-4b52-bde9-f445196b669a",
"user_id": 5444,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "150.00",
"receive": "150.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T14:30:38.000000Z",
"updated_at": "2023-02-16T14:30:38.000000Z"
},
{
"uuid": "7ff5c855-972f-4df0-af09-ded7fc24eb52",
"user_id": 5444,
"type": "buy",
"coin": "ETECSA",
"peer_id": 0,
"amount": "5.00",
"receive": "1000.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T14:28:08.000000Z",
"updated_at": "2023-02-16T14:28:08.000000Z"
},
{
"uuid": "a932eb2f-ae31-4650-9a9a-649bd3cdbe00",
"user_id": 3049,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "100.00",
"receive": "100.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T13:11:14.000000Z",
"updated_at": "2023-02-16T13:11:14.000000Z"
},
{
"uuid": "cbe05f28-67ca-40e7-8570-c2eac9e6ec25",
"user_id": 184969,
"type": "sell",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "2.00",
"receive": "330.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T12:16:07.000000Z",
"updated_at": "2023-02-16T12:16:07.000000Z"
},
{
"uuid": "44cb1dc4-0981-46eb-90de-d207af2ed01b",
"user_id": 184969,
"type": "buy",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "50.00",
"receive": "7950.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T12:15:00.000000Z",
"updated_at": "2023-02-16T12:15:00.000000Z"
},
{
"uuid": "1a7ee635-8fed-4ec9-90d4-bac18ec3a554",
"user_id": 183862,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "10.00",
"receive": "10.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T12:02:53.000000Z",
"updated_at": "2023-02-16T12:02:53.000000Z"
},
{
"uuid": "08f16dda-8371-4e68-8a94-35153e75b99d",
"user_id": 9908,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "20.00",
"receive": "20.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T11:37:18.000000Z",
"updated_at": "2023-02-16T11:37:18.000000Z"
},
{
"uuid": "ccf68eb4-9485-4465-922f-77e7ccdcb780",
"user_id": 21561,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "20.00",
"receive": "19.900000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T11:35:45.000000Z",
"updated_at": "2023-02-16T11:35:45.000000Z"
},
{
"uuid": "84cb180c-2270-43e0-85a6-9804f341007c",
"user_id": 183925,
"type": "buy",
"coin": "ETECSA",
"peer_id": 0,
"amount": "20.00",
"receive": "4400.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T10:46:28.000000Z",
"updated_at": "2023-02-16T10:46:28.000000Z"
},
{
"uuid": "96201fce-eab0-4964-a07f-22bbf3edd316",
"user_id": 183925,
"type": "buy",
"coin": "ETECSA",
"peer_id": 0,
"amount": "15.00",
"receive": "3300.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T10:45:46.000000Z",
"updated_at": "2023-02-16T10:45:46.000000Z"
},
{
"uuid": "50017f37-a9c4-4541-84dc-5d5483fc5127",
"user_id": 183925,
"type": "buy",
"coin": "ETECSA",
"peer_id": 0,
"amount": "5.00",
"receive": "1100.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T10:44:38.000000Z",
"updated_at": "2023-02-16T10:44:38.000000Z"
},
{
"uuid": "54bdc336-4f15-4b36-9f90-2b310ab93bc8",
"user_id": 2242,
"type": "buy",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "30.00",
"receive": "4740.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T10:41:23.000000Z",
"updated_at": "2023-02-16T10:41:23.000000Z"
},
{
"uuid": "af993170-67ed-40a2-a2b8-a40fffa2f1f8",
"user_id": 2242,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "100.00",
"receive": "100.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T10:40:45.000000Z",
"updated_at": "2023-02-16T10:40:45.000000Z"
},
{
"uuid": "6b7419fb-fbef-43ed-aae6-8f3c18497026",
"user_id": 23869,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "50.00",
"receive": "50.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T06:54:55.000000Z",
"updated_at": "2023-02-16T06:54:55.000000Z"
},
{
"uuid": "ab525ae8-ebe3-4d6f-b8c0-d9d645b107d9",
"user_id": 183049,
"type": "buy",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "275.00",
"receive": "275.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T04:02:33.000000Z",
"updated_at": "2023-02-16T04:02:33.000000Z"
},
{
"uuid": "a9f9b165-3b53-443b-a3b1-f4c1581861a3",
"user_id": 55699,
"type": "buy",
"coin": "ETECSA",
"peer_id": 0,
"amount": "19.00",
"receive": "3875.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T03:24:35.000000Z",
"updated_at": "2023-02-16T03:24:35.000000Z"
},
{
"uuid": "a0c35a90-2e5e-46cb-aaa3-eceaf7fd63b9",
"user_id": 55699,
"type": "buy",
"coin": "ETECSA",
"peer_id": 0,
"amount": "10.00",
"receive": "1920.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-16T03:23:31.000000Z",
"updated_at": "2023-02-16T03:23:31.000000Z"
},
{
"uuid": "af66ef84-031b-40b5-a501-896549c9907f",
"user_id": 5533,
"type": "sell",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "8.50",
"receive": "8.850000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-15T21:31:39.000000Z",
"updated_at": "2023-02-15T21:31:39.000000Z"
},
{
"uuid": "daea060d-c33e-4104-99cc-0e069a2cc0d1",
"user_id": 16674,
"type": "sell",
"coin": "RevoluPay",
"peer_id": 0,
"amount": "32.00",
"receive": "30.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-15T19:46:45.000000Z",
"updated_at": "2023-02-15T19:46:45.000000Z"
},
{
"uuid": "5083ec79-dc6b-4d88-9d4c-36eaeed9c08d",
"user_id": 187595,
"type": "sell",
"coin": "TRX",
"peer_id": 0,
"amount": "11.00",
"receive": "164.776981652180",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-14T21:34:28.000000Z",
"updated_at": "2023-02-14T21:34:28.000000Z"
},
{
"uuid": "fd18f1a6-ec76-4312-bbfe-6f48d7abea7b",
"user_id": 184007,
"type": "sell",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "17.50",
"receive": "20.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-13T09:28:08.000000Z",
"updated_at": "2023-02-13T09:28:08.000000Z"
},
{
"uuid": "0014e830-a3e2-404e-a422-91ddfd28eac4",
"user_id": 52529,
"type": "sell",
"coin": "USDT",
"peer_id": 0,
"amount": "2.00",
"receive": "2.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-11T20:34:35.000000Z",
"updated_at": "2023-02-16T07:09:24.000000Z"
},
{
"uuid": "d356b2e5-8cb7-4d85-8da1-1b768f9ba5b1",
"user_id": 1085,
"type": "sell",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "1.20",
"receive": "600.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-11T13:16:54.000000Z",
"updated_at": "2023-02-11T13:16:54.000000Z"
},
{
"uuid": "58add267-2149-428c-94f4-cd540ff13254",
"user_id": 5816,
"type": "sell",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "5.00",
"receive": "6.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-09T00:42:42.000000Z",
"updated_at": "2023-02-09T00:42:42.000000Z"
},
{
"uuid": "558cb193-115d-46cd-905a-feae4817a2c6",
"user_id": 181168,
"type": "sell",
"coin": "BANK_MLC",
"peer_id": 0,
"amount": "19.00",
"receive": "20.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-08T00:42:04.000000Z",
"updated_at": "2023-02-16T02:03:34.000000Z"
},
{
"uuid": "48f710dc-1640-41ae-82b8-96d25acf2705",
"user_id": 52030,
"type": "sell",
"coin": "BANK_CUP",
"peer_id": 0,
"amount": "1.20",
"receive": "600.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2023-02-06T12:46:01.000000Z",
"updated_at": "2023-02-15T20:53:57.000000Z"
},
{
"uuid": "2c0fedbd-06a0-44ef-b139-59c283969a9c",
"user_id": 43658,
"type": "sell",
"coin": "QVAPAY",
"peer_id": 0,
"amount": "7.00",
"receive": "70.000000000000",
"only_kyc": 0,
"private": 0,
"status": "open",
"created_at": "2022-07-12T05:54:11.000000Z",
"updated_at": "2023-02-16T13:35:28.000000Z"
}
],
"first_page_url": "https://qvapay.test/api/p2p/index?page=1",
"from": 1,
"last_page": 1,
"last_page_url": "https://qvapay.test/api/p2p/index?page=1",
"links": [
{
"url": null,
"label": "&laquo; Anterior",
"active": false
},
{
"url": "https://qvapay.test/api/p2p/index?page=1",
"label": "1",
"active": true
},
{
"url": null,
"label": "Siguiente &raquo;",
"active": false
}
],
"next_page_url": null,
"path": "https://qvapay.test/api/p2p/index",
"per_page": 50,
"prev_page_url": null,
"to": 50,
"total": 50
}

Añade la opcion de paginacion tambien
  
---  

*Archivo de instrucciones privado - No subir al repositorio*