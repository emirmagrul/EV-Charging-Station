import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import AddVehicle from './pages/AddVehicle';
import Reservation from './pages/Reservation';
import OperatorDashboard from './pages/OperatorDashboard';
import AdminDashboard from './pages/AdminDashboard';
import { AuthProvider, useAuth } from './context/AuthContext';
import { UIProvider } from './context/UIContext';
import { NotificationProvider } from './context/NotificationContext';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" />;
};

function App() {
  return (
    <AuthProvider>
      <UIProvider>
        <NotificationProvider>
          <Router>
            <div className="app">
              <Navbar />
            <main>
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route 
                  path="/dashboard" 
                  element={
                    <ProtectedRoute>
                      <Dashboard />
                    </ProtectedRoute>
                  } 
                />
                <Route 
                  path="/vehicles/add" 
                  element={
                    <ProtectedRoute>
                      <AddVehicle />
                    </ProtectedRoute>
                  } 
                />
                <Route 
                  path="/reserve/:stationId" 
                  element={
                    <ProtectedRoute>
                      <Reservation />
                    </ProtectedRoute>
                  } 
                />
                 <Route 
                  path="/operator-dashboard" 
                  element={
                    <ProtectedRoute>
                      <OperatorDashboard />
                    </ProtectedRoute>
                  } 
                />
                <Route 
                  path="/admin-dashboard" 
                  element={
                    <ProtectedRoute>
                      <AdminDashboard />
                    </ProtectedRoute>
                  } 
                />
              </Routes>
            </main>

          </div>
        </Router>
        </NotificationProvider>
      </UIProvider>
    </AuthProvider>
  );

}


export default App;
