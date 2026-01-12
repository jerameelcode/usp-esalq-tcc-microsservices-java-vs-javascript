/* eslint-disable @typescript-eslint/no-unsafe-assignment */
/* eslint-disable @typescript-eslint/no-unsafe-member-access */
/* eslint-disable @typescript-eslint/no-unsafe-call */
/* eslint-disable prettier/prettier */

import http from "k6/http";
import { sleep } from "k6";

export const options = {
  vus: 50,
  duration: "10s",
  cloud: {
    projectID: 4764533,
    name: "Test Java (Spring Boot) API - Create User",
  },
};

export default function () {
  const uuid = crypto.randomUUID();
  const name = "User Name " + uuid;
  const email = `user-${uuid}@example.com`;
  const password = uuid;
  console.log(`Creating user: ${name}, ${email}, ${password}`);
  http.post(
    "http://localhost:8081/api/v1/users",
    JSON.stringify({ name, email, password }),
    { headers: { "Content-Type": "application/json" } }
  );

  sleep(1);
}
