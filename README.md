API Documentation
This document provides an overview of the API endpoints available for pdf Ectractor management and file operations.

Base URL
bash
Copy code
http://localhost:8181/api
Endpoints
1. Sign Up
Endpoint: /createuser
Method: POST
Request Body:
json
Copy code
{
    "name": "veerPrakash rathor",
    "email": "2024mt03108@.in",
    "password": "password"
}
2. Sign In
Endpoint: /sign
Method: Get
Request Body:
json
Copy code
{
    "email": "2024mt03108@wilp.bits-pilani.ac.in",
    "password": "password"
}
3. Upload File Pdf/Image
Endpoint: /upload/{email}
Method: POST
URL Example: /upload/2024mt03108@wilp.bits-pilani.ac.in
Description: Upload a file for the specified user.

4. View Data
Endpoint: /getdata/{email}
Method: GET
URL Example: /getdata/2024mt03108@wilp.bits-pilani.ac.in
Description: Retrieve user data associated with the given email.

5. Get PDF
Endpoint: /getPdf/{email}
Method: GET
URL Example: /getPdf/2024mt03108@wilp.bits-pilani.ac.in
Description: Fetch the PDF document associated with the given email.

6. Get Health Status
Endpoint: /actuator/health
Method: GET
Description: Fetch the health status for the application

7. Reset Password
Endpoint: /sign/{email}
Method: PUT
URL Example: /sign/2024mt03108@.in
Description: Reset the password for the specified user.
Response: Returns true upon success.
8. Update User
Endpoint: /updaetuser
Method: PUT
Request Body:
json
Copy code
{
    "name": "veer Prakash",
    "email": "2024mt03108@.in",
    "password": "password"
}
Notes
Ensure that the server is running and accessible at the specified base URL.
Replace 2024mt03108@wilp.bits-pilani.ac.in with the actual user email where applicable.
Example Usage
Sign Up Example
bash
Copy code
curl -X POST http://localhost:8181/api/createuser -H "Content-Type: application/json" -d '{"name": "veerPrakash rathor", "email": "2024mt03108@wilp.bits-pilani.ac.in", "password": "password"}'
Sign In Example
bash
Copy code
curl -X POST http://localhost:8181/api/sign -H "Content-Type: application/json" -d '{"email": "2024mt03108@wilp.bits-pilani.ac.in", "password": "password"}'
