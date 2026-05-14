# ⚡ EV Charging Station Network Management System

![Live Status](https://img.shields.io/badge/Status-Live-success?style=for-the-badge)
![Frontend](https://img.shields.io/badge/Frontend-Vercel-black?style=for-the-badge&logo=vercel)
![Backend](https://img.shields.io/badge/Backend-Render-46E3B7?style=for-the-badge&logo=render)
![Database](https://img.shields.io/badge/Database-Supabase-3ECF8E?style=for-the-badge&logo=supabase)

A comprehensive system to manage an electric vehicle charging station network, handle reservations and payments, and track charging sessions. The system serves EV drivers, station operators, and administrators with role-based access and features.

## 🚀 Live Demo
- **Frontend (Vercel)**: [https://ev-charging-station-lake.vercel.app/](https://ev-charging-station-lake.vercel.app/)
- **Backend API (Render)**: [https://ev-charging-station-8c5y.onrender.com](https://ev-charging-station-8c5y.onrender.com)

---

## 🌟 Key Features

### 🔌 Station Catalog & Charger Types
Maintains a database of charging stations including location details, charger types (AC/DC), power output (22kW, 50kW, 150kW), pricing per kWh, connector types (Type 2, CCS, CHAdeMO), and operating hours.

### 🗺️ Interactive Map & Station Finder
Integrates an interactive map (Leaflet) to display all stations. Users can view their current location, see nearby stations with real-time availability status (**Green:** Available, **Yellow:** Occupied, **Red:** Offline), and get routing directions to the selected station.

### 🚗 Vehicle Registration
Enables users to register their electric vehicles with details (brand, model, battery capacity, connector type, plate number). The system validates vehicle-charger compatibility when making reservations.

### 📅 Charging Reservation & Conflict Handling
Users can select a station on the map and reserve a charging slot. The system enforces rules (e.g., maximum duration, advance booking limits) and prevents double-booking. Includes a strict **No-Show Policy** and automated ghost-session cleanup.

### 💳 Online Payment & Wallet System
Integrates a secure payment system for wallet top-ups. Charging fees are automatically deducted based on kWh consumed. Handles refunds for cancelled reservations dynamically.

### 🔋 Charging Session Tracking
Records active charging sessions with real-time progress (current kWh, estimated time remaining, cost). Users can monitor their session status live.

### 🛠️ Station Maintenance & Issue Reporting
Users can report station issues. Operators can mark chargers as out-of-service, automatically cancelling affected reservations and triggering **real-time notifications** to affected users.

### 📊 Administrative Reporting
Provides a robust Admin Dashboard featuring revenue reports, station utilization statistics, peak hour analysis, and user activity summaries.

---

## 🛠️ Technology Stack

**Frontend:**
- **React.js** (Vite)
- **Vanilla CSS** (Glassmorphism & Modern Responsive UI)
- **Leaflet & Leaflet Routing Machine** (Interactive Map Integration)
- Hosted on **Vercel**

**Backend:**
- **Java Spring Boot 3+**
- **Spring Security & RESTful API Architecture**
- Hosted on **Render**

**Database & Auth:**
- **PostgreSQL** (Relational Data Management)
- **Supabase** (Database Hosting & Authentication)

---

## 👥 User Roles
1. **Driver**: Can register vehicles, search stations on the map, make reservations, track charging sessions, and report faults.
2. **Operator**: Manages specific stations, monitors charger statuses, resolves fault reports, and cancels reservations if maintenance is required.
3. **Admin**: Has full system access, views platform-wide revenue and utilization reports, and manages overall system configuration.

---

## 💻 Local Development Setup

### Prerequisites
- Node.js (v18+) & npm
- Java 17+
- Maven
- PostgreSQL / Supabase Account

### Environment Variables
Before running the application, you need to set up the following environment variables.

**Frontend (`ev-frontend/.env`):**
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

**Backend (`ev-backend/src/main/resources/application.properties`):**
```properties
spring.datasource.url=jdbc:postgresql://<SUPABASE_DB_URL>:6543/postgres
spring.datasource.username=<YOUR_DB_USER>
spring.datasource.password=<YOUR_DB_PASSWORD>
# Add any other JWT/Auth secrets here
```

### ⚙️ Installation & Running

**1. Backend Setup (`/ev-backend`)**
```bash
cd ev-backend
# Install dependencies and run the Spring Boot application
./mvnw spring-boot:run
```
*The backend server will start on `http://localhost:8080`*

**2. Frontend Setup (`/ev-frontend`)**
```bash
cd ev-frontend
# Install dependencies
npm install
# Start the development server
npm run dev
```
*The frontend application will start on `http://localhost:5173`*

---

## 👨‍💻 Author
**Emir Magrul**
- GitHub: [@emirmagrul](https://github.com/emirmagrul)
- LinkedIn: [Emir Magrul](https://www.linkedin.com/in/emirmagrul/)
