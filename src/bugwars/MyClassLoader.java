package bugwars;

class MyClassLoader extends ClassLoader {
	
	MyClassLoader() {
		// use our classloader as a parent, rather than the default
		// system classloader
		super(MyClassLoader.class.getClassLoader());
		
		// always instrument any classes we load
		this.clearAssertionStatus();
		this.setDefaultAssertionStatus(true);
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (Game.printWarnings) System.err.println("Name of Class: " + name);
		return super.loadClass(name, resolve);
	}
}
