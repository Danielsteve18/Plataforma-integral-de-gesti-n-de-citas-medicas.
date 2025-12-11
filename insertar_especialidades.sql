-- =============================================================================
-- SCRIPT PARA INSERTAR ESPECIALIDADES MÉDICAS
-- Sistema: MediPac
-- Base de datos: gestion_citas_medicas
-- =============================================================================

USE gestion_citas_medicas;

-- Insertar especialidades médicas comunes
-- Solo se insertan si no existen (evita duplicados)

INSERT INTO especialidades (nombre, descripcion) VALUES
('Cardiología', 'Especialidad médica que se encarga del corazón y el sistema circulatorio. Diagnóstico y tratamiento de enfermedades cardiovasculares.'),
('Dermatología', 'Especialidad médica enfocada en el cuidado de la piel, cabello y uñas. Tratamiento de enfermedades cutáneas y estéticas.'),
('Endocrinología', 'Especialidad que trata las enfermedades relacionadas con las hormonas y el metabolismo, como diabetes y trastornos tiroideos.'),
('Gastroenterología', 'Especialidad médica del aparato digestivo. Diagnóstico y tratamiento de enfermedades del estómago, intestinos, hígado y páncreas.'),
('Ginecología', 'Especialidad médica y quirúrgica que trata la salud del sistema reproductor femenino.'),
('Medicina General', 'Atención primaria integral para pacientes de todas las edades. Diagnóstico y tratamiento de enfermedades comunes.'),
('Neurología', 'Especialidad médica que trata los trastornos del sistema nervioso central y periférico.'),
('Oftalmología', 'Especialidad médica que se encarga del diagnóstico y tratamiento de las enfermedades de los ojos.'),
('Oncología', 'Especialidad médica dedicada al diagnóstico y tratamiento del cáncer.'),
('Ortopedia', 'Especialidad médica que trata las enfermedades y lesiones del sistema musculoesquelético.'),
('Otorrinolaringología', 'Especialidad médica que trata las enfermedades del oído, nariz y garganta.'),
('Pediatría', 'Especialidad médica dedicada al cuidado de la salud de bebés, niños y adolescentes.'),
('Psiquiatría', 'Especialidad médica que se ocupa del diagnóstico, prevención y tratamiento de trastornos mentales.'),
('Psicología', 'Especialidad que estudia el comportamiento humano y los procesos mentales. Terapia y apoyo psicológico.'),
('Neumología', 'Especialidad médica que se encarga del diagnóstico y tratamiento de enfermedades del sistema respiratorio.'),
('Urología', 'Especialidad médica que trata las enfermedades del tracto urinario y sistema reproductor masculino.'),
('Traumatología', 'Especialidad médica que trata las lesiones traumáticas del sistema musculoesquelético.'),
('Radiología', 'Especialidad médica que utiliza técnicas de imagen para el diagnóstico y tratamiento de enfermedades.'),
('Anestesiología', 'Especialidad médica dedicada a la atención y cuidado de los pacientes durante procedimientos quirúrgicos.'),
('Medicina Interna', 'Especialidad médica que se dedica a la atención integral del adulto enfermo, enfocándose en el diagnóstico y tratamiento no quirúrgico.')
ON DUPLICATE KEY UPDATE nombre=nombre;

-- Verificar las especialidades insertadas
SELECT COUNT(*) as total_especialidades FROM especialidades;
SELECT * FROM especialidades ORDER BY nombre;



