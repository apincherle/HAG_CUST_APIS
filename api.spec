openapi: 3.0.3
info:
  title: Grading Platform API
  version: 1.0.0
servers:
  - url: https://api.yourdomain.com

paths:
  /v1/customers:
    post:
      summary: Create customer
      operationId: createCustomer
      tags: [Customers]
      parameters:
        - in: header
          name: Idempotency-Key
          required: false
          schema: { type: string, maxLength: 128 }
          description: Optional key to safely retry create without duplicates.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CustomerCreateRequest'
            examples:
              example:
                value:
                  email: "ash@example.com"
                  full_name: "Ash Ketchum"
                  phone: "+44 7700 900123"
                  billing_address:
                    line1: "1 Example Street"
                    city: "London"
                    postcode: "SW1A 1AA"
                    country: "GB"
                  shipping_address:
                    line1: "1 Example Street"
                    city: "London"
                    postcode: "SW1A 1AA"
                    country: "GB"
                  marketing_opt_in: false
      responses:
        '201':
          description: Created
          headers:
            Location:
              schema: { type: string }
              description: URL of the created customer resource.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Customer' }
        '400': { $ref: '#/components/responses/BadRequest' }
        '409': { $ref: '#/components/responses/Conflict' }

    get:
      summary: List/search customers
      operationId: listCustomers
      tags: [Customers]
      parameters:
        - in: query
          name: q
          schema: { type: string, maxLength: 200 }
          description: Full-text-ish search across name/email/phone (implementation-dependent).
        - in: query
          name: email
          schema: { type: string, format: email }
          description: Filter by exact email.
        - in: query
          name: phone
          schema: { type: string, maxLength: 50 }
          description: Filter by phone (exact or normalized match).
        - in: query
          name: status
          schema:
            type: string
            enum: [ACTIVE, DELETED]
          description: Filter by customer status.
        - in: query
          name: limit
          schema: { type: integer, minimum: 1, maximum: 200, default: 50 }
        - in: query
          name: cursor
          schema: { type: string, maxLength: 256 }
          description: Opaque pagination cursor returned by previous call.
        - in: query
          name: sort
          schema:
            type: string
            enum: [created_at, email, full_name]
            default: created_at
        - in: query
          name: order
          schema:
            type: string
            enum: [asc, desc]
            default: desc
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CustomerListResponse' }
        '400': { $ref: '#/components/responses/BadRequest' }

  /v1/customers:lookup:
    get:
      summary: Lookup customer by email (single)
      operationId: lookupCustomerByEmail
      tags: [Customers]
      parameters:
        - in: query
          name: email
          required: true
          schema: { type: string, format: email }
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Customer' }
        '404': { $ref: '#/components/responses/NotFound' }
        '400': { $ref: '#/components/responses/BadRequest' }

  /v1/customers/{customer_id}:
    get:
      summary: Get customer by id
      operationId: getCustomer
      tags: [Customers]
      parameters:
        - $ref: '#/components/parameters/CustomerId'
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Customer' }
        '404': { $ref: '#/components/responses/NotFound' }

    patch:
      summary: Update customer (partial)
      operationId: updateCustomer
      tags: [Customers]
      parameters:
        - $ref: '#/components/parameters/CustomerId'
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/CustomerUpdateRequest' }
            examples:
              example:
                value:
                  full_name: "Ash Ketchum"
                  phone: "+44 7700 900999"
                  shipping_address:
                    line1: "2 New Street"
                    city: "London"
                    postcode: "SW1A 2AA"
                    country: "GB"
      responses:
        '200':
          description: Updated
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Customer' }
        '400': { $ref: '#/components/responses/BadRequest' }
        '404': { $ref: '#/components/responses/NotFound' }
        '409': { $ref: '#/components/responses/Conflict' }

    delete:
      summary: Delete customer (soft delete)
      operationId: deleteCustomer
      tags: [Customers]
      parameters:
        - $ref: '#/components/parameters/CustomerId'
      responses:
        '204':
          description: Deleted
        '404': { $ref: '#/components/responses/NotFound' }
        '409':
          description: Conflict (e.g., customer has active submissions and you disallow delete)
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Error' }

  /v1/customers/{customer_id}:restore:
    post:
      summary: Restore soft-deleted customer
      operationId: restoreCustomer
      tags: [Customers]
      parameters:
        - $ref: '#/components/parameters/CustomerId'
      responses:
        '200':
          description: Restored
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Customer' }
        '404': { $ref: '#/components/responses/NotFound' }
        '409': { $ref: '#/components/responses/Conflict' }

components:
  parameters:
    CustomerId:
      in: path
      name: customer_id
      required: true
      schema:
        type: string
        format: uuid
      description: Customer GUID.

  responses:
    BadRequest:
      description: Bad request
      content:
        application/json:
          schema: { $ref: '#/components/schemas/Error' }
    NotFound:
      description: Not found
      content:
        application/json:
          schema: { $ref: '#/components/schemas/Error' }
    Conflict:
      description: Conflict (duplicate, state conflict, etc.)
      content:
        application/json:
          schema: { $ref: '#/components/schemas/Error' }

  schemas:
    Address:
      type: object
      additionalProperties: false
      properties:
        line1: { type: string, maxLength: 200 }
        line2: { type: string, maxLength: 200, nullable: true }
        city: { type: string, maxLength: 120 }
        region: { type: string, maxLength: 120, nullable: true }
        postcode: { type: string, maxLength: 20 }
        country: { type: string, minLength: 2, maxLength: 2, description: "ISO-3166 alpha-2" }
      required: [line1, city, postcode, country]

    Customer:
      type: object
      additionalProperties: false
      properties:
        customer_id: { type: string, format: uuid }
        email: { type: string, format: email }
        phone: { type: string, nullable: true, maxLength: 50 }
        full_name: { type: string, maxLength: 200 }
        billing_address: { $ref: '#/components/schemas/Address', nullable: true }
        shipping_address: { $ref: '#/components/schemas/Address', nullable: true }
        marketing_opt_in: { type: boolean, default: false }
        status:
          type: string
          enum: [ACTIVE, DELETED]
        created_at: { type: string, format: date-time }
        updated_at: { type: string, format: date-time }
        deleted_at: { type: string, format: date-time, nullable: true }
      required: [customer_id, email, full_name, status, created_at, updated_at]

    CustomerCreateRequest:
      type: object
      additionalProperties: false
      properties:
        email: { type: string, format: email }
        phone: { type: string, maxLength: 50, nullable: true }
        full_name: { type: string, maxLength: 200 }
        billing_address: { $ref: '#/components/schemas/Address', nullable: true }
        shipping_address: { $ref: '#/components/schemas/Address', nullable: true }
        marketing_opt_in: { type: boolean, default: false }
      required: [email, full_name]

    CustomerUpdateRequest:
      type: object
      additionalProperties: false
      properties:
        email:
          type: string
          format: email
          description: "Optional. If allowed, requires uniqueness check."
        phone: { type: string, maxLength: 50, nullable: true }
        full_name: { type: string, maxLength: 200 }
        billing_address: { $ref: '#/components/schemas/Address', nullable: true }
        shipping_address: { $ref: '#/components/schemas/Address', nullable: true }
        marketing_opt_in: { type: boolean }
      description: Any subset of fields to update.

    CustomerListResponse:
      type: object
      additionalProperties: false
      properties:
        items:
          type: array
          items: { $ref: '#/components/schemas/Customer' }
        next_cursor:
          type: string
          nullable: true
          description: Opaque cursor for next page (null if no more).
      required: [items, next_cursor]

    Error:
      type: object
      additionalProperties: false
      properties:
        code: { type: string, example: "VALIDATION_ERROR" }
        message: { type: string }
        details:
          type: object
          nullable: true
      required: [code, message]
