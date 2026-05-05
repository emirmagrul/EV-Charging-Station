import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';

function App() {
  return (
    <Router>
      <div className="app">
        <Navbar />
        <main style={{ padding: '20px' }}>
          <Routes>
            <Route path="/" element={<Home />} />
            {/* Diğer sayfalar (Login, Register vb.) sırayla eklenecek */}
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
