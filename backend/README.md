# NewsPortal Backend

Backend API para el portal de noticias automático construido con Spring Boot 3.

## Tecnologías

- **Java 17**
- **Spring Boot 3.2**
- **Spring Security** con JWT
- **Spring Data JPA** con PostgreSQL
- **Redis** para caché
- **RabbitMQ** para colas de mensajes
- **Flyway** para migraciones de base de datos
- **Maven** para gestión de dependencias

## Estructura del Proyecto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/newsportal/
│   │   │   ├── api/              # REST Controllers
│   │   │   ├── model/            # Entidades JPA
│   │   │   ├── repository/       # Repositorios Spring Data
│   │   │   ├── service/          # Lógica de negocio
│   │   │   ├── security/         # Configuración de seguridad y JWT
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   └── NewsPortalApplication.java
│   │   └── resources/
│   │       ├── application.yml   # Configuración
│   │       └── db/migration/     # Migraciones Flyway
│   └── test/
├── pom.xml
└── Dockerfile
```

## Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- PostgreSQL 15+
- Redis 7+
- RabbitMQ 3.12+

## Configuración

1. **Copiar el archivo de variables de entorno:**
   ```bash
   cp ../.env.example ../.env
   ```

2. **Editar `.env` con tus credenciales:**
   - Configurar credenciales de base de datos
   - Agregar claves API para servicios de IA (OpenAI, etc.)
   - Configurar secreto JWT
   - Configurar credenciales S3

## Ejecutar con Docker Compose

La forma más fácil de ejecutar todo el stack:

```bash
# Desde la raíz del proyecto
docker-compose up -d
```

Esto iniciará:
- PostgreSQL en puerto 5432
- Redis en puerto 6379
- RabbitMQ en puerto 5672 (Management UI en 15672)

## Ejecutar Localmente (Desarrollo)

1. **Asegúrate de que PostgreSQL, Redis y RabbitMQ estén corriendo**

2. **Compilar el proyecto:**
   ```bash
   mvn clean install
   ```

3. **Ejecutar la aplicación:**
   ```bash
   mvn spring-boot:run
   ```

La API estará disponible en `http://localhost:8080`

## Endpoints Principales

### Autenticación
- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/login` - Iniciar sesión

### Noticias (Público)
- `GET /api/news` - Listar noticias publicadas
- `GET /api/news/{id}` - Ver noticia específica
- `GET /api/news/search?keyword=...` - Buscar noticias
- `GET /api/news/tags?tags=...` - Filtrar por tags

### Noticias (Autenticado - EDITOR/ADMIN)
- `POST /api/news` - Crear noticia
- `PUT /api/news/{id}` - Actualizar noticia
- `DELETE /api/news/{id}` - Eliminar noticia (solo ADMIN)
- `POST /api/news/{id}/publish` - Publicar noticia
- `POST /api/news/{id}/reject` - Rechazar noticia

### Comentarios
- `GET /api/news/{newsId}/comments` - Listar comentarios
- `POST /api/news/{newsId}/comments` - Crear comentario (requiere autenticación)
- `DELETE /api/news/{newsId}/comments/{id}` - Eliminar comentario
- `POST /api/news/{newsId}/comments/{id}/report` - Reportar comentario

### Backoffice (EDITOR/ADMIN/MODERATOR)
- `GET /api/backoffice/pending` - Noticias pendientes de aprobación
- `POST /api/backoffice/news/{id}/approve` - Aprobar noticia
- `POST /api/backoffice/news/{id}/reject` - Rechazar noticia
- `GET /api/backoffice/comments/unmoderated` - Comentarios sin moderar
- `POST /api/backoffice/comments/{id}/approve` - Aprobar comentario
- `POST /api/backoffice/comments/{id}/reject` - Rechazar comentario

## Autenticación

La API usa JWT (JSON Web Tokens) para autenticación.

1. **Registrarse o iniciar sesión** para obtener un token
2. **Incluir el token** en el header de las peticiones:
   ```
   Authorization: Bearer <tu-token-jwt>
   ```

## Usuarios por Defecto

Después de ejecutar las migraciones, se crean dos usuarios por defecto:

- **Admin:**
  - Username: `admin`
  - Password: `admin123`
  - Roles: ROLE_ADMIN

- **Editor:**
  - Username: `editor`
  - Password: `editor123`
  - Roles: ROLE_EDITOR

> ⚠️ **IMPORTANTE:** Cambia estas contraseñas en producción!

## Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests con cobertura
mvn test jacoco:report
```

## Build para Producción

```bash
# Compilar JAR
mvn clean package

# Ejecutar JAR
java -jar target/newsportal-backend-0.0.1-SNAPSHOT.jar
```

## Docker

```bash
# Construir imagen
docker build -t newsportal-backend .

# Ejecutar contenedor
docker run -p 8080:8080 --env-file ../.env newsportal-backend
```

## Próximos Pasos

- [ ] Implementar servicios de IA (scraper, rewriter, image generation)
- [ ] Agregar tests de integración
- [ ] Configurar CI/CD
- [ ] Implementar rate limiting
- [ ] Agregar documentación Swagger/OpenAPI
- [ ] Implementar WebSockets para comentarios en tiempo real

## Troubleshooting

### Error de conexión a la base de datos
- Verifica que PostgreSQL esté corriendo
- Verifica las credenciales en `.env`
- Asegúrate de que la base de datos `newsportal` exista

### Error de migraciones Flyway
- Limpia la base de datos y vuelve a ejecutar
- Verifica que no haya migraciones corruptas en `db/migration`

### Error de JWT
- Verifica que `JWT_SECRET` esté configurado en `.env`
- Asegúrate de que el secret tenga al menos 256 bits

## Licencia

MIT
