import { useState, useEffect } from 'react';
import chargerService from '../services/chargerService';

export const useStations = () => {
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchStations = async () => {
    setLoading(true);
    try {
      const data = await chargerService.getAllStations();
      setStations(data);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStations();
  }, []);

  return { stations, loading, error, refreshStations: fetchStations };
};
