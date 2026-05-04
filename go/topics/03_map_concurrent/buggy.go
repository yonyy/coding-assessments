// Package mapconcurrent demonstrates unsynchronized concurrent map access.
//
// COACHING PHRASE: "Go maps are not goroutine-safe. Any concurrent write —
// even to a different key — is undefined behavior and will panic with
// 'concurrent map read and map write'."
//
// SPOT THE BUGS:
//  1. AddRoute() writes to m.routes from multiple goroutines with no lock.
//  2. Route() reads m.routes without a lock — concurrent with writes it races.
//  3. Remove() also unguarded.
package mapconcurrent

type RequestRouter struct {
	routes map[string]string // tunnelID -> backendAddr
}

func NewRequestRouter() *RequestRouter {
	return &RequestRouter{routes: make(map[string]string)}
}

// Bug: no synchronization. Two concurrent AddRoute calls will race.
func (r *RequestRouter) AddRoute(tunnelID, addr string) {
	r.routes[tunnelID] = addr
}

// Bug: concurrent read during a write panics at runtime.
func (r *RequestRouter) Route(tunnelID string) (string, bool) {
	addr, ok := r.routes[tunnelID]
	return addr, ok
}

func (r *RequestRouter) Remove(tunnelID string) {
	delete(r.routes, tunnelID)
}
