package de.sk.jetty;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import io.bootique.ConfigModule;
import io.bootique.jersey.JerseyModule;

/**
 * LogServer module starting point 
 *
 */
public class App extends ConfigModule
{
    public static void main( String[] args )
    {		
		Module module = binder -> {
			Multibinder<Object> resList = JerseyModule.contributeResources(binder);
			resList.addBinding().to(Resource.class);
		};
		
		Bootique.app(args).module(module).modules(App.class).autoLoadModules().run();
		
    }
    
    @Override
    public void configure(Binder binder) {
    	super.configure(binder);
		BQCoreModule.contributeCommands(binder).addBinding().to(StartCommand.class);

    	binder.bind(JsonMapper.class).in(Scopes.SINGLETON);
   }    
}
