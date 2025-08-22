import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import GamePage from './pages/GamePage';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';
import { AuthProvider, useAuth } from './context/AuthContext';
import './App.css';

function Navigation() {
    const { isAuthenticated, logout } = useAuth();
    return (
        <nav>
            <Link to="/">Играть</Link>
            {isAuthenticated ? (
                <>
                    <Link to="/profile">Профиль</Link>
                    <button onClick={logout} className="logout-btn">Выйти</button>
                </>
            ) : (
                <Link to="/login">Войти</Link>
            )}
        </nav>
    );
}


function App() {
    return (
        <AuthProvider>
            <Router>
                <div className="app">
                    <header>
                        <Navigation />
                    </header>
                    <main>
                        <Routes>
                            <Route path="/" element={<GamePage />} />
                            <Route path="/login" element={<LoginPage />} />
                            <Route path="/profile" element={<ProfilePage />} />
                        </Routes>
                    </main>
                </div>
            </Router>
        </AuthProvider>
    );
}

export default App;