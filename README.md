# The purpuse of this project. 
This Android application leverages Bluetooth Low Energy (BLE) technology to accurately determine the distance between a transmitter and the mobile device. 
This proximity-based functionality enhances user experiences in various settings

For instance, in a museum, visitors can effortlessly access detailed information about an artifact simply by approaching it, eliminating the need for physical signage or guided tours.
Similarly, in hospitals, healthcare professionals can seamlessly view personalized data relevant to their current location, such as patient information within a specific room or on a particular floor.

## How it Works

### Receiving Signals:

The application leverages the AltBeacon library to capture Bluetooth Low Energy (BLE) signals from nearby transmitters. It then extracts the Received Signal Strength Indicator (RSSI), which represents the signal strength, to estimate the distance between the device and the transmitter. However, RSSI alone is unreliable due to potential signal fluctuations in wireless transmissions. ()

### Enhancing Accuracy with Machine Learning:

To address this limitation, the application employs a K-Nearest Neighbors (KNN) machine learning algorithm. This choice prioritizes on-device processing for real-time results. By analyzing a set of recent RSSI values, the KNN algorithm significantly improves distance estimation accuracy to nearly 98%.

### Data Pipeline:

1. Data Acquisition: The application receives RSSI data from BLE signals by SignalHandler.java .
2. Distance Calculation: The RSSI value is converted into an estimated distance by SignalHandler.java .
3. Data Storage: The distance data, along with an identifier for the signal source (e.g., point A), is stored in a local SQLite database managed by DbManager.java.
4. Machine Learning Integration: Once a sufficient number of RSSI values (e.g., 10) are collected, the KNN algorithm is triggered to analyze the data and provide a refined distance estimate by KnnAlgoFilter.java.
5. Result Display: The application displays the distance calculated by the KNN algorithm, leveraging the most recent data points.
