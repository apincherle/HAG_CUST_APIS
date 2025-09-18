// MongoDB initialization script
// This script runs when the MongoDB container starts for the first time

// Switch to the placementdb database
db = db.getSiblingDB('placementdb');

// Create a user for the application
db.createUser({
  user: 'appuser',
  pwd: 'apppassword',
  roles: [
    {
      role: 'readWrite',
      db: 'placementdb'
    }
  ]
});

// Create collections with validation
db.createCollection('placements', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['_id', '_metadata'],
      properties: {
        _id: {
          bsonType: 'string',
          description: 'must be a string and is required'
        },
        _metadata: {
          bsonType: 'object',
          description: 'must be an object and is required'
        }
      }
    }
  }
});

db.createCollection('users');
db.createCollection('documents');
db.createCollection('programmes');
db.createCollection('contracts');
db.createCollection('sections');
db.createCollection('risks');
db.createCollection('limits');
db.createCollection('premiums');
db.createCollection('insureds');
db.createCollection('branches');
db.createCollection('brokerTeams');
db.createCollection('underwriterPools');
db.createCollection('metadata');
db.createCollection('companies');
db.createCollection('organizations');

// Create indexes for better performance
db.placements.createIndex({ "_id": 1 });
db.placements.createIndex({ "user._xid": 1 });
db.placements.createIndex({ "status": 1 });
db.placements.createIndex({ "effective_year": 1 });

db.users.createIndex({ "_xid": 1 });
db.users.createIndex({ "first_name": 1, "last_name": 1 });

db.documents.createIndex({ "_xid": 1 });
db.documents.createIndex({ "name": 1 });

print('MongoDB initialization completed successfully!');
