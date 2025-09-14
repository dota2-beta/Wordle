import apiClient from './axiosConfig';

export const createGame = () => {
    return apiClient.post('/api/games/create');
};

export const makeGuess = (gameId, guess) => {
    return apiClient.post('/api/games/guess', { gameId, guess });
};