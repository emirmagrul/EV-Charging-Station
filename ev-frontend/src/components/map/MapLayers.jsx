import { useEffect, useRef } from 'react';
import { useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet-routing-machine';

export const RoutingMachine = ({ userCoords, targetCoords, setRouteInfo }) => {
  const map = useMap();
  const routingControlRef = useRef(null);

  useEffect(() => {
    if (!map || !userCoords || !targetCoords) return;
    if (routingControlRef.current) map.removeControl(routingControlRef.current);

    routingControlRef.current = L.Routing.control({
      waypoints: [
        L.latLng(userCoords.latitude, userCoords.longitude),
        L.latLng(targetCoords.latitude, targetCoords.longitude)
      ],
      lineOptions: { styles: [{ color: '#10b981', weight: 6, opacity: 0.8 }] },
      createMarker: () => null,
      addWaypoints: false,
      draggableWaypoints: false,
      fitSelectedRoutes: true,
      show: false
    }).on('routesfound', function(e) {
      const summary = e.routes[0].summary;
      setRouteInfo({
        distance: (summary.totalDistance / 1000).toFixed(1),
        time: Math.round(summary.totalTime / 60)
      });
    }).addTo(map);

    return () => { if (routingControlRef.current) map.removeControl(routingControlRef.current); };
  }, [map, userCoords, targetCoords]);

  return null;
};

export const ChangeView = ({ center, zoom }) => {
  const map = useMap();
  useEffect(() => {
    if (center) map.flyTo(center, zoom, { animate: true, duration: 1 });
  }, [center, zoom, map]);
  return null;
};
