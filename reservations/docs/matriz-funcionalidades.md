# Matriz de Funcionalidades y Evidencias - AndesStay MQ

**Proyecto:** AndesStay Asynchronous Messaging & Microservices  
**Asignatura:** Cloud Native  
**Evaluación:** Evaluación Parcial N°3  
**Plataforma:** Spring Boot 3, RabbitMQ, Docker

---

## 1. Topología y Componentes de Mensajería

| Componente | Tipo | Routing Key / Binding | Propósito / Dominio |
| :--- | :--- | :--- | :--- |
| `ex.andesstay.direct` | Direct Exchange | - | Enrutamiento de comandos principales |
| `ex.andesstay.dlx` | Direct Exchange (DLX) | `k.cmd.dlq` | Enrutamiento de mensajes fallidos a DLQ |
| `q.cmd.email` | Queue | `k.cmd.email.send` | Notificaciones y correos a huéspedes |
| `q.cmd.email.dlq` | Dead Letter Queue | `k.cmd.dlq` | Captura de fallos de procesamiento de correos |
| `q.cmd.housekeeping` | Queue | `k.cmd.housekeeping.ticket` | Generación de tickets de limpieza |
| `q.cmd.housekeeping.dlq` | Dead Letter Queue | `k.cmd.dlq` | Captura de fallos en tickets de limpieza |
| `q.cmd.voucher` | Queue | `k.cmd.voucher.gen` | Generación de vouchers y comprobantes |
| `q.cmd.voucher.dlq` | Dead Letter Queue | `k.cmd.dlq` | Captura de fallos en emisión de vouchers |

---

## 2. Matriz de Cumplimiento de Criterios

| Criterio Evaluado | Microservicio Responsable | Archivos Clave de Implementación | Evidencia Verificable |
| :--- | :--- | :--- | :--- |
| **Nombres centralizados de colas y exchanges** | `ms-andesstay-reservations`<br>`ms-andesstay-notify` | `src/main/resources/application.properties` | Propiedades parametrizadas bajo el prefijo `andesstay.rabbitmq.*` sin hardcodeo en código. |
| **Declaración de Beans (Queues, DLQs, Bindings)** | `ms-andesstay-reservations` | `com.andesstay.reservations.config.RabbitMQConfig` | Declaración de 2 Exchanges, 3 colas de comandos con argumentos `x-dead-letter-*`, 3 DLQs y sus respectivos `BindingBuilder`. |
| **Separación de lógica de negocio y mensajería** | `ms-andesstay-reservations` | `com.andesstay.reservations.producer.ReservationMessageProducer` | Clase productora dedicada que desacopla la persistencia (`ReservationController`) de la publicación en RabbitMQ (`RabbitTemplate`). |
| **Consumidores por dominio con `@RabbitListener`** | `ms-andesstay-notify` | `com.andesstay.notify.consumer.NotificationMessageConsumer` | 3 métodos `@RabbitListener` independientes para `email`, `housekeeping` y `voucher`. |
| **ACK manual y desvío a DLQ** | `ms-andesstay-notify` | `NotificationMessageConsumer`<br>`application.properties` | Configuración `acknowledge-mode=manual`. Procesamiento con `channel.basicAck()` en éxito y `channel.basicNack(deliveryTag, false, false)` en fallo para desviar a DLQ. |
| **Endpoints REST de administración (POST/DELETE)** | `ms-andesstay-admin-mq` | `com.andesstay.adminmq.controller.QueueAdminController` | Endpoints `POST /api/admin/queues` para declarar colas y `DELETE /api/admin/queues/{name}` para purgarlas/eliminarlas. |
| **Lógica encapsulada en `RabbitAdminService`** | `ms-andesstay-admin-mq` | `com.andesstay.adminmq.service.RabbitAdminService` | Uso de `org.springframework.amqp.rabbit.core.RabbitAdmin` para operaciones programáticas sobre el broker. |
| **Validación de DTOs en administración** | `ms-andesstay-admin-mq` | `com.andesstay.adminmq.dto.QueueRequestDto` | Uso de anotaciones `@NotBlank`, `@Pattern` y validación activa mediante `@Valid` en el controlador. |

---

## 3. Pruebas de Flujo End-to-End

1. **Creación de Reserva:**  
   Se envía `POST /api/reservations`. El productor emite un envelope con `NotificationPayload` a `q.cmd.email`. El listener de `notify` lo consume y emite un ACK con confirmación de log.
2. **Confirmación de Reserva:**  
   Se envía `PUT /api/reservations/{id}/status` con `{"status": "CONFIRMADA"}`. Se disparan simultáneamente los 3 eventos (`email`, `housekeeping`, `voucher`), consumidos en paralelo por `ms-andesstay-notify`.
3. **Prueba de Resiliencia / DLQ:**  
   Al enviar un payload sin correo válido a `q.cmd.email`, el consumidor captura la excepción, invoca `basicNack(..., false)` y RabbitMQ redirige el mensaje a `q.cmd.email.dlq`.
4. **Gestión Administrativa:**  
   Se realiza `POST /api/admin/queues` con body `{"name": "q.test.evaluacion"}`. La cola se declara inmediatamente en RabbitMQ Management.