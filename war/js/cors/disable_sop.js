if (navigator.userAgent.indexOf("Firefox") != -1) {
    try {
        netscape.security.PrivilegeManager.enablePrivilege("UniversalBrowserRead");
    } 
    catch (e) {
        alert("Permission UniversalBrowserRead denied -- not running Mozilla?");
    }
}
