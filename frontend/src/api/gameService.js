import apiClient from './axiosConfig';

export const createGame = () => {
    return apiClient.get('/games/create');
};

export const makeGuess = (gameId, guess) => {
    return apiClient.post('/games/guess', { gameId, guess });
};