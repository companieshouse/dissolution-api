db.api_clients.insert({
  "_id" : "1234567890.apps.ch.gov.uk",
  "app_name" : "CHS",
  "client_secret" : "M2UwYzRkNzIwOGQ1OGQ0OWIzMzViYjJjOTEyYTc1",
  "user_id" : "Y2VkZWVlMzhlZWFjY2M4MzQ3MT",
  "redirect_uris" : [
    "http://chs-dev/oauth2/user/callback"
  ],
  "type" : "web",
  "is_internal_app": true
})

db.api_clients.insert({
  "_id" : "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz",
  "type" : "key",
  "app_name" : "ch.gov.uk API key",
  "user_id" : "Y2VkZWVlMzhlZWFjY2M4MzQ3MT",
  "can_fetch_api_client" : 1,
  "can_fetch_bearer_token" : 1,
  "restricted_ips" : [],
  "js_domains" : []
})

db.users.insert({
  "_id" : "Y2VkZWVlMzhlZWFjY2M4MzQ3MT",
  "surname" : null,
  "locale" : "GB_en",
  "password" : "$2a$10$6a..eerV1kSiNW3sBlcYv.VmEXyI7ABWuoo3w7zKzcdh18YKyvPbm",
  "forename" : null,
  "email" : "demo@ch.gov.uk",
  "created" : { "$date" : 1420070400000 }
})
