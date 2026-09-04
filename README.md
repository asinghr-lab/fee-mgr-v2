# School Fee Management — Phase 1

This baseline implements **Module 1: Identity**.

Package convention for every module:
- `com.discover.app.<module-name>.domain`
- `com.discover.app.<module-name>.repository`
- `com.discover.app.<module-name>.service`
- `com.discover.app.<module-name>.dto`
- `com.discover.app.<module-name>.web`
- `com.discover.app.<module-name>.api`

Identity entities:
- User
- UserRole
- UserProfile
- Role

Seed users:
- admin / admin123
- staff / staff123

Run:
`mvn clean test`
then:
`mvn spring-boot:run`

Login:
`http://localhost:8080/login`

REST:
`GET /api/identity/me`

The remaining modules will be added one at a time on top of this baseline.
