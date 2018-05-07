package sapphire.common;

import java.net.InetSocketAddress;

import static sapphire.common.Configuration.ProcessEntity.SERVER;

/**
 * Created by SMoon on 2/27/2018.
 */

public class Configuration {
    public final static String WhereToProcessPrefix = "Will process on ";
    public static ProcessEntity WhereToProcess = SERVER;

    // Whether to use IP address or DNS name for connecting to KernelServer.
    // For cloud computing environment, it should use public DNS name.
    // For internal network, it may need to use IP address.
    public static boolean useIpAddress = true;

    public static String getWhereToProcess() {
        return WhereToProcessPrefix + WhereToProcess.toString();
    }

    public enum ProcessEntity { DEVICE, SERVER, UNDECIDED };

    public static String[] natOmsAddress = { "10.8.0.1", "22343" };
    public static String[] natServerKernelAddress = { "10.8.0.1", "22345" };
    public static String[] natDeviceKernelAddress = { "10.8.0.2", "31111" };

    public static InetSocketAddress getNatServerKernelAddress() {
        return new InetSocketAddress(natServerKernelAddress[0], Integer.parseInt(natServerKernelAddress[1]));
    }
    public static InetSocketAddress getNatDeviceKernelAddress() {
        return new InetSocketAddress(natDeviceKernelAddress[0], Integer.parseInt(natDeviceKernelAddress[1]));
    }

    public static String getNatOmsString() {
        // String.join requires high API level (26).
        return natOmsAddress[0] + ":" + natOmsAddress[1];
    }

    public static String omsUsage = "Usage: hostname (or IP address):port \nE.g. 10.8.0.1:22343";
}
