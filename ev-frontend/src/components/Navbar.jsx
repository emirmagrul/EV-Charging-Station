import React from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  return (
    <div className="navbar-container">
      <nav className="navbar glass-panel">
        <Link to="/" className="nav-brand">
          ⚡ <span>EV</span> Charger
        </Link>

        <div className="nav-links">
          <Link to="/" className="nav-link">Ana Sayfa</Link>
          <button className="btn-primary">Giriş Yap</button>
        </div>
      </nav>
    </div>
  );
};

export default Navbar;
