import React, { createContext, useState, useEffect, useContext } from 'react';
import { getProfile } from '../api/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const loadUserFromToken = async () => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const response = await getProfile();
                setUser(response.data);
            } catch (error) {
                localStorage.removeItem('token');
                setUser(null);
            }
        }
        setLoading(false);
    };

    useEffect(() => {
        loadUserFromToken();
    }, []);

    const login = async (token) => {
        localStorage.setItem('token', token);
        await loadUserFromToken(); 
    };

    const logout = () => {
        localStorage.removeItem('token');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);