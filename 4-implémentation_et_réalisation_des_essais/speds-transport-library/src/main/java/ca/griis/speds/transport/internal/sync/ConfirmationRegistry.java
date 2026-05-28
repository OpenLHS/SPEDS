package ca.griis.speds.transport.internal.sync;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * "Description brève du composant (classe, interface, ...)"
 *
 * <h3>Historique</h3>
 * <p>
 * XXXX-XX-XX [AS] - Implémentation initiale<br>
 * </p>
 *
 * <h3>Tâches</h3>
 * S.O.
 *
 * @author [AS] ameni.souid@usherbrooke.ca
 * @since
 */
public class ConfirmationRegistry {
  private final ConcurrentHashMap<UUID, CompletableFuture<String>> pending =
      new ConcurrentHashMap<>();

  public CompletableFuture<String> register(UUID id) {
    CompletableFuture<String> future = new CompletableFuture<>();
    CompletableFuture<String> existing = pending.putIfAbsent(id, future);

    if (existing != null) {
      throw new IllegalStateException("Confirmation already registered for id: " + id);
    }

    return future;
  }

  public CompletableFuture<String> remove(UUID id) {
    return pending.remove(id);
  }

  public void confirm(UUID id, String value) {
    CompletableFuture<String> future = remove(id);
    if (future != null) {
      future.complete(value);
    }
  }

  public void cleanUp() {
    pending.clear();
  }
}
