import apiClient from './axiosConfig';

export const register = (userData) => {
    return apiClient.post('/api/auth/register', userData);
};

export const login = (credentials) => {
    return apiClient.post('/api/auth/authenticate', credentials);
};

export const getProfile = () => {
    return apiClient.get('/api/users/profile');
}

export const getMyRank = () => {
    return apiClient.get('/api/users/me/rank');
};

export const getTopPlayers = () => {
    return apiClient.get('/api/users/top');
};