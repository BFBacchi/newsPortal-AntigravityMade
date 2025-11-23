# 🚀 NewsPortal - Portal de Noticias Automatizado con IA

Portal de noticias totalmente automático con frontend en **React** y backend en **Java Spring Boot**. Sistema que encuentra noticias en internet, las reescribe con IA mediante prompts, genera imágenes relacionadas, ofrece backoffice para moderación/edición, autenticación, comentarios y generación automatizada de placas para redes sociales.

## ✨ Características

### Backend (Java Spring Boot)
- ✅ **API REST** completa con Spring Boot 3.2
- ✅ **Autenticación JWT** con roles (ADMIN, EDITOR, USER)
- ✅ **Integración con LLMs** (OpenAI GPT-4, Anthropic Claude)
- ✅ **Generación de imágenes** con IA (DALL-E, Stability AI)
- ✅ **Sistema de Jobs asíncrono** con RabbitMQ
- ✅ **Almacenamiento S3** compatible
- ✅ **Base de datos PostgreSQL** con Flyway migrations
- ✅ **Cache con Redis**
- ✅ **Sistema de auditoría** completo
- ✅ **Comentarios** con moderación

### Frontend (React + Vite)
- ✅ **Diseño moderno** con Tailwind CSS
- ✅ **Animaciones fluidas** con Framer Motion
- ✅ **Glassmorphism** y efectos neon
- ✅ **Responsive** para todos los dispositivos
- ✅ **React Query** para gestión de estado
- ✅ **React Router** para navegación

## 🛠️ Tecnologías

### Backend
- Java 17
- Spring Boot 3.2
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Redis
- RabbitMQ
- Flyway
- AWS S3 SDK
- Lombok

### Frontend
- React 18
- Vite
- Tailwind CSS
- Framer Motion
- React Router DOM
- TanStack React Query
- Axios

## 📦 Instalación

### Prerrequisitos
- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Redis
- RabbitMQ
- (Opcional) S3-compatible storage

### 1. Clonar el repositorio
```bash
git clone <repository-url>
cd "Punto de partida medios - app"
```

### 2. Configurar Backend

#### Crear base de datos
```sql
CREATE DATABASE newsportal;
CREATE USER newsportal_user WITH PASSWORD 'newsportal_pass';
GRANT ALL PRIVILEGES ON DATABASE newsportal TO newsportal_user;
```

#### Configurar variables de entorno
Copiar `.env.example` y crear `.env`:
```bash
cp .env.example .env
```

Editar `.env` con tus credenciales:
```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=newsportal
DB_USER=newsportal_user
DB_PASSWORD=newsportal_pass

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=newsportal
RABBITMQ_PASSWORD=newsportal_pass

# JWT
JWT_SECRET=your-super-secret-jwt-key-min-256-bits
JWT_EXPIRATION_MS=86400000

# OpenAI
OPENAI_API_KEY=sk-your-openai-key

# DALL-E (puede ser la misma que OpenAI)
DALLE_API_KEY=sk-your-openai-key

# S3 Storage (opcional)
S3_ENDPOINT=https://your-s3-endpoint.com
S3_BUCKET=newsportal-media
S3_ACCESS_KEY=your-access-key
S3_SECRET_KEY=your-secret-key
S3_REGION=us-east-1
```

#### Compilar y ejecutar
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

El backend estará disponible en `http://localhost:8080`

### 3. Configurar Frontend

#### Instalar dependencias
```bash
cd frontend
npm install
```

#### Configurar variables de entorno
```bash
cp .env.example .env
```

Editar `.env`:
```properties
VITE_API_URL=http://localhost:8080/api
```

#### Ejecutar en desarrollo
```bash
npm run dev
```

El frontend estará disponible en `http://localhost:5173`

## 🚀 Uso

### Iniciar servicios con Docker Compose

```bash
docker-compose up -d
```

Esto iniciará:
- PostgreSQL en puerto 5432
- Redis en puerto 6379
- RabbitMQ en puerto 5672 (Management UI en 15672)

### Acceder a la aplicación

1. **Frontend**: http://localhost:5173
2. **Backend API**: http://localhost:8080/api
3. **RabbitMQ Management**: http://localhost:15672 (usuario: newsportal, password: newsportal_pass)

### Usuario por defecto

El sistema crea un usuario administrador por defecto:
- **Username**: admin
- **Password**: admin123

⚠️ **IMPORTANTE**: Cambiar estas credenciales en producción.

## 📚 API Endpoints

### Autenticación
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/register` - Registrar usuario
- `GET /api/auth/me` - Obtener usuario actual

### Noticias
- `GET /api/news` - Listar noticias (con paginación)
- `GET /api/news/{id}` - Obtener noticia por ID
- `POST /api/news` - Crear noticia
- `PUT /api/news/{id}` - Actualizar noticia
- `DELETE /api/news/{id}` - Eliminar noticia
- `POST /api/news/{id}/publish` - Publicar noticia

### Comentarios
- `GET /api/news/{newsId}/comments` - Listar comentarios
- `POST /api/news/{newsId}/comments` - Crear comentario
- `DELETE /api/news/{newsId}/comments/{commentId}` - Eliminar comentario

### Backoffice (requiere rol ADMIN o EDITOR)
- `GET /api/backoffice/auto-generated` - Listar noticias auto-generadas pendientes
- `POST /api/backoffice/news/{id}/approve` - Aprobar noticia
- `POST /api/backoffice/news/{id}/reject` - Rechazar noticia

## 🔄 Sistema de Jobs

El sistema utiliza RabbitMQ para procesar trabajos asíncronos:

1. **news_rewrite** - Reescribe artículos usando LLM
2. **image_generation** - Genera imágenes con IA
3. **social_card_generation** - Crea placas para redes sociales

### Publicar un job manualmente

```java
@Autowired
private JobPublisher jobPublisher;

// Reescribir noticia
jobPublisher.publishNewsRewriteJob(newsId);

// Generar imagen
jobPublisher.publishImageGenerationJob(newsId);

// Pipeline completo
jobPublisher.publishCompletePipeline(newsId);
```

## 🎨 Personalización del Frontend

### Colores del tema

Editar `frontend/tailwind.config.js`:

```javascript
theme: {
  extend: {
    colors: {
      primary: { /* tus colores */ },
      accent: { /* tus colores */ },
    }
  }
}
```

### Animaciones

Las animaciones están definidas en `frontend/src/index.css` usando Tailwind y Framer Motion.

## 📝 Próximos Pasos

- [ ] Implementar servicio de scraping de noticias
- [ ] Agregar scheduler para automatización completa
- [ ] Integración con redes sociales (Twitter, Facebook, Instagram)
- [ ] Panel de analytics y métricas
- [ ] Sistema de notificaciones en tiempo real con WebSockets
- [ ] Tests E2E con Playwright
- [ ] CI/CD con GitHub Actions
- [ ] Deployment en Kubernetes

## 🤝 Contribuir

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## ⚖️ Consideraciones Legales

⚠️ **IMPORTANTE**: Este proyecto es para fines educativos y de demostración.

Antes de usar en producción:
- Revisar leyes de copyright de tu país
- Implementar políticas de atribución
- Respetar `robots.txt` de los sitios fuente
- Implementar moderación humana
- Marcar claramente contenido generado por IA
- Consultar con un abogado especializado

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Ver `LICENSE` para más detalles.

## 👥 Autores

- Tu Nombre - Desarrollo inicial

## 🙏 Agradecimientos

- OpenAI por GPT-4 y DALL-E
- Anthropic por Claude
- Spring Boot team
- React team
- Tailwind CSS team

---

**Hecho con ❤️ y ☕**
