package ru.mirea.muravievvr.mireaproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import ru.mirea.muravievvr.mireaproject.databinding.ActivityMapsBinding;

public class MapsActivity extends AppCompatActivity implements DrivingSession.DrivingRouteListener {
    private ActivityMapsBinding binding;
    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private PlacemarkMapObject chosenObject;
    private LocationManager locationManager;
    private MapObjectTapListener tapListener;
    private Point myLocation;
    private final int[] colors = {0xFFFF0000, 0xFF00FF00, 0x00FFBBBB, 0xFF0000FF};
    private final java.util.Map<PlacemarkMapObject, String> names = new HashMap<>();
    private List<PolylineMapObject> lastLines = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String MAPKIT_API_KEY = "d8cdd37d-1f9b-422b-a7e3-e571ff1b4708";
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        checkPermissions();

        mapView = binding.mapview;
        mapView.getMap().setNightModeEnabled(true);

        locationManager = MapKitFactory.getInstance().createLocationManager();
        LocationListener myLocationListener = new LocationListener() {
            @Override
            public void onLocationUpdated(@NonNull Location location) {
                myLocation = location.getPosition();
            }

            @Override
            public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {
            }
        };
        locationManager.subscribeForLocationUpdates(
                0, 0, 25.0, false, FilteringMode.ON, myLocationListener);

        loadUserLocationLayer();
        mapView.getMap().setRotateGesturesEnabled(false);
        mapView.getMap().move(
                new CameraPosition(new Point(55.751574, 37.573856), 11.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0),
                null);

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        tapListener = (mapObject, point) -> {
            if (mapObject instanceof PlacemarkMapObject) {
                for (int i = 0; i < lastLines.size(); i++) {
                    mapObjects.remove(lastLines.get(i));
                }
                lastLines.clear();
                if (drivingSession != null) drivingSession.cancel();

                binding.btnRoute.setEnabled(true);
                chosenObject = (PlacemarkMapObject) mapObject;
                binding.tvPlacemark.setText(names.get(chosenObject));
                binding.tvAddress.setText(getAddress(point));
            }
            return false;
        };

        addPlaces();

        binding.btnRoute.setEnabled(false);
        binding.btnRoute.setOnClickListener(v -> {
            if (chosenObject != null ) {
                submitRequest();
            }
        });
    }

    private void loadUserLocationLayer(){
        MapKit mapKit = MapKitFactory.getInstance();
        mapKit.setLocationManager(locationManager);
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(false);
    }

    private void checkPermissions() {
        int loc1PermissionStatus = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int loc2PermissionStatus = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (loc1PermissionStatus == PackageManager.PERMISSION_GRANTED &&
                loc2PermissionStatus == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 100);
        }
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
        int color;
        for (int i = 0; i < list.size(); i++) {
            color = colors[i];
            PolylineMapObject line = mapObjects.addPolyline(list.get(i).getGeometry());
            line.setStrokeColor(color);
            lastLines.add(line);
        }
    }
    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void submitRequest() {
        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();
        drivingOptions.setRoutesCount(4);
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        try {
            requestPoints.add(new RequestPoint(Objects.requireNonNull(userLocationLayer.cameraPosition()).getTarget(),
                    RequestPointType.WAYPOINT,
                    null));
            requestPoints.add(new RequestPoint(chosenObject.getGeometry(),
                    RequestPointType.WAYPOINT,
                    null));
            drivingSession = drivingRouter.requestRoutes(requestPoints, drivingOptions,
                    vehicleOptions, this);
        } catch (NullPointerException e) {
            Toast.makeText(this, "Wait for finding your location", Toast.LENGTH_SHORT).show();
        }

    }

    private void addPlaces() {
        PlacemarkMapObject pt = mapObjects.addPlacemark(new Point(55.79402, 37.69978));
        pt.setText("РТУ МИРЕА");
        pt.setIcon(ImageProvider.fromResource(this, R.drawable.mirea),
                new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(1f)
                        .setScale(0.05f));
        pt.addTapListener(tapListener);
        names.put(pt, "РТУ МИРЕА");

        PlacemarkMapObject pt2 = mapObjects.addPlacemark(new Point(55.92611, 37.70978));
        pt2.setText("Волейболная площадка в лесу");
        pt2.setIcon(ImageProvider.fromResource(this, R.drawable.forest),
                new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(1f)
                        .setScale(0.05f));
        pt2.addTapListener(tapListener);
        names.put(pt2, "Волейболная площадка в лесу");

        PlacemarkMapObject pt3 = mapObjects.addPlacemark(new Point(55.89101, 37.68235));
        pt3.setText("Парк на Яузе");
        pt3.setIcon(ImageProvider.fromResource(this, R.drawable.park),
                new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(1f)
                        .setScale(0.05f));
        pt3.addTapListener(tapListener);
        names.put(pt3, "Парк на Яузе");
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
    }

    private String getAddress(Point location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String address = "Error";
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                address = returnedAddress.getAddressLine(0);
            } else {
                Toast.makeText(this, "Null address", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in getting address", Toast.LENGTH_SHORT).show();
        }
        return address;
    }
}