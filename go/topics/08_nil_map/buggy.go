// Package nilmap demonstrates the nil map write panic.
//
// COACHING PHRASE: "Map fields are nil in the zero value of a struct. Reading
// a nil map is safe and returns the zero value. Writing to a nil map panics
// immediately with 'assignment to entry in nil map'."
//
// SPOT THE BUGS:
//  1. TunnelRegistry has a map field that is never initialized.
//  2. Register() writes to it — runtime panic if the struct is zero-value initialized.
//  3. Zero value of a struct containing a map is NOT ready to use without a constructor.
package nilmap

type TunnelRegistry struct {
	tunnels map[string]string // nil until explicitly initialized
}

// Bug: panics if TunnelRegistry was created as TunnelRegistry{} (zero value).
func (r *TunnelRegistry) Register(id, addr string) {
	r.tunnels[id] = addr // panic: assignment to entry in nil map
}

// Safe: reading a nil map returns zero value, no panic.
func (r *TunnelRegistry) Lookup(id string) (string, bool) {
	addr, ok := r.tunnels[id]
	return addr, ok
}
