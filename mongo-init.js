db = db.getSiblingDB('rinha');
db.createUser({
    user: 'root',
    pwd: 'root',
    roles: [{role: 'readWrite', db: 'rinha'}]
});

db.createCollection("client")
db.client.insertMany([
    {_id: 1, limit: 100000, amount: 0},
    {_id: 2, limit: 80000, amount: 0},
    {_id: 3, limit: 1000000, amount: 0},
    {_id: 4, limit: 10000000, amount: 0},
    {_id: 5, limit: 500000, amount: 0}
])