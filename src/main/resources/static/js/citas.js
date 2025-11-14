/**
 * Funciones JavaScript para la gestión de citas médicas
 */

// Obtener CSRF token
function getCsrfToken() {
    const meta = document.querySelector('meta[name="_csrf"]');
    return meta ? meta.getAttribute('content') : '';
}

function getCsrfHeader() {
    const meta = document.querySelector('meta[name="_csrf_header"]');
    return meta ? meta.getAttribute('content') : '';
}

// Cargar doctores por especialidad
async function cargarDoctoresPorEspecialidad(especialidad, selectElement) {
    if (!especialidad) {
        selectElement.innerHTML = '<option value="">Primero seleccione una especialidad...</option>';
        selectElement.disabled = true;
        return;
    }

    try {
        selectElement.innerHTML = '<option value="">Cargando doctores...</option>';
        selectElement.disabled = true;

        const response = await fetch(`/api/doctores/especialidad/${especialidad}`);
        const doctores = await response.json();

        if (doctores.length === 0) {
            selectElement.innerHTML = '<option value="">No hay doctores disponibles</option>';
            return;
        }

        selectElement.innerHTML = '<option value="">Seleccione un doctor...</option>';
        doctores.forEach(doctor => {
            const option = document.createElement('option');
            option.value = doctor.id;
            option.textContent = `Dr. ${doctor.nombre} ${doctor.apellido}`;
            option.dataset.info = JSON.stringify(doctor);
            selectElement.appendChild(option);
        });

        selectElement.disabled = false;
    } catch (error) {
        console.error('Error al cargar doctores:', error);
        selectElement.innerHTML = '<option value="">Error al cargar doctores</option>';
    }
}

// Cargar horarios disponibles
async function cargarHorariosDisponibles(doctorId, fecha, containerElement) {
    if (!doctorId || !fecha) {
        containerElement.innerHTML = '<p class="alert alert-info">Seleccione un doctor y una fecha</p>';
        return;
    }

    try {
        containerElement.innerHTML = '<p class="alert alert-info">Cargando horarios...</p>';

        const response = await fetch(`/api/citas/disponibles?doctorId=${doctorId}&fecha=${fecha}`);
        const horarios = await response.json();

        if (horarios.length === 0) {
            containerElement.innerHTML = '<p class="alert alert-error">No hay horarios disponibles para esta fecha</p>';
            return;
        }

        containerElement.innerHTML = '';
        horarios.forEach(horario => {
            const slot = document.createElement('div');
            slot.className = 'horario-slot';
            slot.textContent = horario;
            slot.dataset.horario = horario;
            
            slot.addEventListener('click', function() {
                document.querySelectorAll('.horario-slot').forEach(s => s.classList.remove('selected'));
                this.classList.add('selected');
                
                // Actualizar input hidden si existe
                const horarioInput = document.getElementById('horario');
                if (horarioInput) {
                    horarioInput.value = this.dataset.horario;
                }
                
                // Habilitar botón de submit si existe
                const submitBtn = document.getElementById('submitBtn');
                if (submitBtn) {
                    submitBtn.disabled = false;
                }
            });
            
            containerElement.appendChild(slot);
        });
    } catch (error) {
        console.error('Error al cargar horarios:', error);
        containerElement.innerHTML = '<p class="alert alert-error">Error al cargar horarios</p>';
    }
}

// Confirmar cita
async function confirmarCita(citaId) {
    if (!confirm('¿Confirmar esta cita?')) {
        return false;
    }

    try {
        const token = getCsrfToken();
        const header = getCsrfHeader();

        const response = await fetch(`/api/citas/${citaId}/confirmar`, {
            method: 'POST',
            headers: {
                [header]: token,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            showMessage('Cita confirmada exitosamente', 'success');
            setTimeout(() => location.reload(), 1500);
            return true;
        } else {
            const error = await response.text();
            showMessage('Error al confirmar la cita: ' + error, 'error');
            return false;
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Error al confirmar la cita', 'error');
        return false;
    }
}

// Completar cita
async function completarCita(citaId) {
    if (!confirm('¿Marcar esta cita como completada?')) {
        return false;
    }

    try {
        const token = getCsrfToken();
        const header = getCsrfHeader();

        const response = await fetch(`/api/citas/${citaId}/completar`, {
            method: 'POST',
            headers: {
                [header]: token,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            showMessage('Cita completada exitosamente', 'success');
            setTimeout(() => location.reload(), 1500);
            return true;
        } else {
            const error = await response.text();
            showMessage('Error al completar la cita: ' + error, 'error');
            return false;
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Error al completar la cita', 'error');
        return false;
    }
}

// Cancelar cita
async function cancelarCita(citaId) {
    if (!confirm('¿Está seguro de cancelar esta cita?')) {
        return false;
    }

    try {
        const token = getCsrfToken();
        const header = getCsrfHeader();

        const response = await fetch(`/api/citas/${citaId}/cancelar`, {
            method: 'POST',
            headers: {
                [header]: token,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            showMessage('Cita cancelada exitosamente', 'success');
            setTimeout(() => location.reload(), 1500);
            return true;
        } else {
            const error = await response.text();
            showMessage('Error al cancelar la cita: ' + error, 'error');
            return false;
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Error al cancelar la cita', 'error');
        return false;
    }
}

// Mostrar mensaje
function showMessage(message, type = 'success') {
    // Buscar o crear contenedor de mensajes
    let container = document.querySelector('.alert');
    if (!container) {
        container = document.createElement('div');
        const contentContainer = document.querySelector('.cita-container') || 
                                 document.querySelector('.citas-container') ||
                                 document.querySelector('.agenda-container') ||
                                 document.body.firstChild;
        contentContainer.parentNode.insertBefore(container, contentContainer);
    }
    
    container.className = `alert alert-${type}`;
    container.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i> ${message}`;
    
    // Auto-hide después de 3 segundos
    setTimeout(() => {
        if (container.parentNode) {
            container.remove();
        }
    }, 3000);
}

// Formatear fecha
function formatearFecha(fecha) {
    const opciones = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(fecha).toLocaleDateString('es-ES', opciones);
}

// Formatear hora
function formatearHora(hora) {
    return hora.substring(0, 5); // HH:mm
}

// Validar fecha (no puede ser en el pasado)
function validarFecha(fechaInput) {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    
    const fechaSeleccionada = new Date(fechaInput);
    
    if (fechaSeleccionada < hoy) {
        showMessage('No puede seleccionar una fecha en el pasado', 'error');
        return false;
    }
    
    return true;
}

// Exportar funciones para uso global
if (typeof window !== 'undefined') {
    window.CitasHelper = {
        cargarDoctoresPorEspecialidad,
        cargarHorariosDisponibles,
        confirmarCita,
        completarCita,
        cancelarCita,
        showMessage,
        formatearFecha,
        formatearHora,
        validarFecha
    };
}
