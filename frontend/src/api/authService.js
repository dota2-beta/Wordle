import apiClient from './axiosConfig';

export const register = (userData) => {
    return apiClient.post('/auth/register', userData);
};

export const login = (credentials) => {
    return apiClient.post('/auth/authenticate', credentials);
};

export const getProfile = () => {
    return apiClient.get('/users/profile');
}