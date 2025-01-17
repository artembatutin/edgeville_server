package net.arrav.net.database.connection;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import net.arrav.net.database.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * An abstracted class which handles the connection pool usages.
 * @author Artem Batutin <artembatutin@gmail.com>
 */
public abstract class ConnectionUse {
	
	/**
	 * The connection pool used in the context.
	 */
	private final ConnectionPool pool;
	
	/**
	 * Constructs a new pool usage.
	 * @param pool the pool being used.
	 */
	public ConnectionUse(ConnectionPool pool) {
		this.pool = pool;
	}
	
	/**
	 * Submits the usage.
	 */
	public void submit() {
		if(pool == null)
			return;
		try {
			Futures.addCallback(pool.obtainConnection(), new FutureCallback<Connection>() {
				@Override
				public void onFailure(Throwable arg0) {
					onError();
				}
				
				@Override
				public void onSuccess(Connection arg0) {
					try {
						try(Connection conn = arg0) {
							if(conn == null) {
								onError();
							} else {
								append(conn);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
						onFailure(e);
					}
				}
			});
		} catch(Exception e) {
			e.printStackTrace();
			onError();
		}
	}
	
	/**
	 * Appending the connection.
	 * @param con the connection being used.
	 */
	public abstract void append(Connection con) throws SQLException;
	
	/**
	 * Process handled on an error.
	 */
	public abstract void onError();
}
