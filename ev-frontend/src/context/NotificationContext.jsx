import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useAuth } from './AuthContext';
import notificationService from '../services/notificationService';

const NotificationContext = createContext();

export const NotificationProvider = ({ children }) => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchNotifications = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    try {
      const isOperator = user.role === 'OPERATOR';
      const data = isOperator 
        ? await notificationService.getOperatorNotifications(user.id)
        : await notificationService.getNotifications(user.id);
      setNotifications(data);
      
      const count = isOperator
        ? await notificationService.getOperatorUnreadCount(user.id)
        : await notificationService.getUnreadCount(user.id);
      setUnreadCount(count);
    } catch (err) {
      console.error("Bildirimler yüklenemedi", err);
    } finally {
      setLoading(false);
    }
  }, [user]);

  const markAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (err) {
      console.error("Bildirim okundu işaretlenemedi", err);
    }
  };

  const markAllAsRead = async () => {
    if (!user) return;
    try {
      if (user.role === 'OPERATOR') {
        await notificationService.markAllOperatorAsRead(user.id);
      } else {
        await notificationService.markAllAsRead(user.id);
      }
      setNotifications(prev => prev.map(n => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error("Hepsi okundu işaretlenemedi:", err);
    }
  };

  useEffect(() => {
    if (user) {
      fetchNotifications();
      // Opsiyonel: Polling eklenebilir
      const interval = setInterval(fetchNotifications, 30000); // 30 saniyede bir kontrol et
      return () => clearInterval(interval);
    } else {
      setNotifications([]);
      setUnreadCount(0);
    }
  }, [user, fetchNotifications]);

  return (
    <NotificationContext.Provider value={{
      notifications,
      unreadCount,
      loading,
      fetchNotifications,
      markAsRead,
      markAllAsRead
    }}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};
