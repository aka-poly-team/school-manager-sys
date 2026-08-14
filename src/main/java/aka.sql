-- =================================================================================
-- AKA SYSTEM DATABASE CREATION & SEED DATA SCRIPT (SQL SERVER / AZURE SQL)
-- Môi trường: SQL Server 2016+ / Azure SQL Database / LocalDB
-- Tác giả: AKA Team
-- Phiên bản: 2.0 (Đồng bộ hoàn toàn với các Entity Java Spring Boot)
-- =================================================================================

USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'aka')
BEGIN
    CREATE DATABASE aka;
END
GO

USE aka;
GO

-- =============================================
-- 1. DỌN DẸP BẢNG CŨ NẾU ĐÃ TỒN TẠI (DROP TABLES)
-- =============================================
IF OBJECT_ID('dbo.PasswordResetTokens', 'U') IS NOT NULL DROP TABLE dbo.PasswordResetTokens;
IF OBJECT_ID('dbo.DocumentTemplates', 'U') IS NOT NULL DROP TABLE dbo.DocumentTemplates;
IF OBJECT_ID('dbo.SystemLogs', 'U')        IS NOT NULL DROP TABLE dbo.SystemLogs;
IF OBJECT_ID('dbo.Notifications', 'U')     IS NOT NULL DROP TABLE dbo.Notifications;
IF OBJECT_ID('dbo.Complaints', 'U')        IS NOT NULL DROP TABLE dbo.Complaints;
IF OBJECT_ID('dbo.ChangeRequests', 'U')    IS NOT NULL DROP TABLE dbo.ChangeRequests;
IF OBJECT_ID('dbo.Attendance', 'U')        IS NOT NULL DROP TABLE dbo.Attendance;
IF OBJECT_ID('dbo.Schedules', 'U')         IS NOT NULL DROP TABLE dbo.Schedules;
IF OBJECT_ID('dbo.Classes', 'U')           IS NOT NULL DROP TABLE dbo.Classes;
IF OBJECT_ID('dbo.Schools', 'U')           IS NOT NULL DROP TABLE dbo.Schools;
IF OBJECT_ID('dbo.Users', 'U')             IS NOT NULL DROP TABLE dbo.Users;
IF OBJECT_ID('dbo.Teachers', 'U')          IS NOT NULL DROP TABLE dbo.Teachers;
IF OBJECT_ID('dbo.Roles', 'U')             IS NOT NULL DROP TABLE dbo.Roles;
GO

-- =============================================
-- 2. TẠO CÁC BẢNG NỀN TẢNG (CREATE TABLES)
-- =============================================


-- BẢNG 1: GIÁO VIÊN (Teachers)
CREATE TABLE dbo.Teachers (
    id          INT IDENTITY(1,1)   NOT NULL,
    name        NVARCHAR(255)       NOT NULL,
    dob         DATE                NULL,
    phone       VARCHAR(20)         NULL,
    email       VARCHAR(255)        NULL,
    address     NVARCHAR(500)       NULL,
    status      VARCHAR(20)         NOT NULL DEFAULT 'active',
    CONSTRAINT PK_Teachers PRIMARY KEY CLUSTERED (id ASC)
);
GO

-- BẢNG 2: TÀI KHOẢN NGUỜI DÙNG (Users)
CREATE TABLE dbo.Users (
    id          INT IDENTITY(1,1)   NOT NULL,
    username    VARCHAR(255)        NOT NULL,
    password    VARCHAR(255)        NOT NULL,
    role        VARCHAR(50)         NOT NULL DEFAULT 'ROLE_TEACHER',
    enabled     BIT                 NOT NULL DEFAULT 1,
    teacherId   INT                 NULL,
    avatarUrl   VARCHAR(500)        NULL,
    CONSTRAINT PK_Users PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_Users_Username UNIQUE (username),
    CONSTRAINT FK_Users_Teachers FOREIGN KEY (teacherId) REFERENCES dbo.Teachers(id) ON DELETE SET NULL
);
GO

-- BẢNG 3: TRƯỜNG MẦM NON (Schools)
CREATE TABLE dbo.Schools (
    id            INT IDENTITY(1,1) NOT NULL,
    name          NVARCHAR(255)     NOT NULL,
    address       NVARCHAR(500)     NULL,
    contactPerson NVARCHAR(255)     NULL,
    phone         VARCHAR(20)       NULL,
    CONSTRAINT PK_Schools PRIMARY KEY CLUSTERED (id ASC)
);
GO

-- BẢNG 4: LỚP HỌC AEROBIC (Classes)
CREATE TABLE dbo.Classes (
    id              INT IDENTITY(1,1) NOT NULL,
    name            NVARCHAR(255)     NOT NULL,
    schoolId        INT               NOT NULL,
    studentCount    INT               NOT NULL DEFAULT 20,
    standardPeriods INT               NOT NULL DEFAULT 2,
    CONSTRAINT PK_Classes PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_Classes_Schools FOREIGN KEY (schoolId) REFERENCES dbo.Schools(id) ON DELETE CASCADE
);
GO

-- BẢNG 5: LỊCH GIẢNG DẠY TUẦN (Schedules)
CREATE TABLE dbo.Schedules (
    id              INT IDENTITY(1,1) NOT NULL,
    dayOfWeek       INT               NOT NULL, -- 2: Thứ 2, ..., 8: Chủ nhật
    session         NVARCHAR(50)      NOT NULL, -- Ca 1, Ca 2, Ca 3, Ca 4
    teacherId       INT               NOT NULL,
    schoolId        INT               NOT NULL,
    classId         INT               NOT NULL,
    periods         INT               NOT NULL DEFAULT 2,
    startTime       TIME(0)           NOT NULL,
    endTime         TIME(0)           NOT NULL,
    CONSTRAINT PK_Schedules PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_Schedules_Teachers FOREIGN KEY (teacherId) REFERENCES dbo.Teachers(id) ON DELETE CASCADE,
    CONSTRAINT FK_Schedules_Schools  FOREIGN KEY (schoolId)  REFERENCES dbo.Schools(id)  ON DELETE NO ACTION,
    CONSTRAINT FK_Schedules_Classes  FOREIGN KEY (classId)   REFERENCES dbo.Classes(id)  ON DELETE NO ACTION
);
GO

-- BẢNG 6: NHẬT KÝ ĐIỂM DANH & CHẤM CÔNG (Attendance) - Id BIGINT để khớp với Long trong Attendance.java
CREATE TABLE dbo.Attendance (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    date        DATE                 NOT NULL,
    scheduleId  INT                  NULL,
    teacherId   INT                  NOT NULL,
    schoolId    INT                  NOT NULL,
    classId     INT                  NOT NULL,
    session     NVARCHAR(50)         NOT NULL,
    checkInTime TIME(0)             NOT NULL,
    periods     INT                  NOT NULL DEFAULT 2,
    selfieImage VARCHAR(500)         NULL,
    notes       NVARCHAR(MAX)        NULL,
    status      VARCHAR(20)          NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    CONSTRAINT PK_Attendance PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_Attendance_Schedules FOREIGN KEY (scheduleId) REFERENCES dbo.Schedules(id) ON DELETE SET NULL,
    CONSTRAINT FK_Attendance_Teachers  FOREIGN KEY (teacherId)  REFERENCES dbo.Teachers(id)  ON DELETE NO ACTION,
    CONSTRAINT FK_Attendance_Schools   FOREIGN KEY (schoolId)   REFERENCES dbo.Schools(id)   ON DELETE NO ACTION,
    CONSTRAINT FK_Attendance_Classes   FOREIGN KEY (classId)    REFERENCES dbo.Classes(id)    ON DELETE NO ACTION
);
GO

-- BẢNG 7: YÊU CẦU XIN NGHĨ & DẠY THAY (ChangeRequests)
CREATE TABLE dbo.ChangeRequests (
    id                  INT IDENTITY(1,1) NOT NULL,
    teacherId           INT               NOT NULL,
    requestType         VARCHAR(50)       NOT NULL, -- LEAVE, SUBSTITUTE, CHANGE
    date                DATE              NOT NULL,
    session             NVARCHAR(50)      NOT NULL,
    scheduleId          INT               NULL,
    reason              NVARCHAR(MAX)     NOT NULL,
    status              VARCHAR(20)       NOT NULL DEFAULT 'pending', -- pending, approved, rejected
    createdAt           DATETIME          NOT NULL DEFAULT GETDATE(),
    adminNotes          NVARCHAR(MAX)     NULL,
    documentUrl         VARCHAR(500)      NULL,
    CONSTRAINT PK_ChangeRequests PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_ChangeRequests_Teachers  FOREIGN KEY (teacherId)  REFERENCES dbo.Teachers(id)  ON DELETE NO ACTION,
    CONSTRAINT FK_ChangeRequests_Schedules FOREIGN KEY (scheduleId) REFERENCES dbo.Schedules(id) ON DELETE SET NULL
);
GO

-- BẢNG 8: KHIẾU NẠI CHẤM CÔNG (Complaints) - attendanceId BIGINT khớp với Attendance.id
CREATE TABLE dbo.Complaints (
    id              INT IDENTITY(1,1) NOT NULL,
    attendanceId    BIGINT            NOT NULL,
    content         NVARCHAR(MAX)     NOT NULL,
    expectedPeriods INT               NOT NULL DEFAULT 2,
    status          INT               NOT NULL DEFAULT 0, -- 0: Chờ xử lý, 1: Đã giải quyết, 2: Từ chối
    adminNotes      NVARCHAR(MAX)     NULL,
    resolvedAt      DATETIME          NULL,
    CONSTRAINT PK_Complaints PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_Complaints_Attendance FOREIGN KEY (attendanceId) REFERENCES dbo.Attendance(id) ON DELETE CASCADE
);
GO

-- BẢNG 9: THÔNG BÁO HỆ THỐNG (Notifications) - Id BIGINT để khớp với Long trong Notification.java
CREATE TABLE dbo.Notifications (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    message     NVARCHAR(MAX)        NOT NULL,
    link        VARCHAR(255)         NULL,
    forAdmin    BIT                  NOT NULL DEFAULT 0,
    teacherId   INT                  NULL,
    isRead      BIT                  NOT NULL DEFAULT 0,
    createdAt   DATETIME             NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_Notifications PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_Notifications_Teachers FOREIGN KEY (teacherId) REFERENCES dbo.Teachers(id) ON DELETE CASCADE
);
GO

-- BẢNG 10: NHẬT KÝ HỆ THỐNG (SystemLogs)
CREATE TABLE dbo.SystemLogs (
    id          INT IDENTITY(1,1)   NOT NULL,
    userId      INT                 NULL,
    role        VARCHAR(50)         NULL,
    action      NVARCHAR(255)       NOT NULL,
    details     NVARCHAR(MAX)       NULL,
    timestamp   DATETIME            NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_SystemLogs PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_SystemLogs_Users FOREIGN KEY (userId) REFERENCES dbo.Users(id) ON DELETE SET NULL
);
GO

-- BẢNG 11: BIỂU MẪU & CHỨNG TỪ (DocumentTemplates) - Cập nhật đồng bộ cột với DocumentTemplate.java
CREATE TABLE dbo.DocumentTemplates (
    id            INT IDENTITY(1,1)   NOT NULL,
    name          NVARCHAR(255)       NOT NULL,
    description   NVARCHAR(MAX)       NULL,
    fileName      NVARCHAR(255)       NULL,
    fileType      VARCHAR(50)         NOT NULL DEFAULT 'DOCX',
    fileUrl       VARCHAR(500)        NOT NULL,
    downloadCount INT                 NOT NULL DEFAULT 0,
    createdAt     DATETIME            NOT NULL DEFAULT GETDATE(),
    userId        INT                 NULL,
    CONSTRAINT PK_DocumentTemplates PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_DocumentTemplates_Users FOREIGN KEY (userId) REFERENCES dbo.Users(id) ON DELETE SET NULL
);
GO

-- BẢNG 12: TOKEN ĐẶT LẠI MẬT KHẨU (PasswordResetTokens)
CREATE TABLE dbo.PasswordResetTokens (
    id          INT IDENTITY(1,1)   NOT NULL,
    token       VARCHAR(255)        NOT NULL,
    userId      INT                 NOT NULL,
    expiryDate  DATETIME            NOT NULL,
    CONSTRAINT PK_PasswordResetTokens PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_PasswordResetTokens_Token UNIQUE (token),
    CONSTRAINT FK_PasswordResetTokens_Users FOREIGN KEY (userId) REFERENCES dbo.Users(id) ON DELETE CASCADE
);
GO

-- =============================================
-- DỮ LIỆU MẪU BAN ĐẦU (FULL SEED DATA DEMO - 20 BẢN GHI / BẢNG)
-- NGUYÊN TẮC: GIỮ NGUYÊN 100% CÁC TÀI KHOẢN USERS CỦA NGƯỜI DÙNG
-- =============================================

-- Mật khẩu mặc định cho tất cả tài khoản: 123456
-- Hash BCrypt: $2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS

-- 1. Thêm Giáo viên (Giữ nguyên các bản ghi nếu đã có)
IF NOT EXISTS (SELECT 1 FROM dbo.Teachers WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.Teachers ON;
    INSERT INTO dbo.Teachers (id, name, dob, phone, email, address, status) VALUES
    (1, N'Nguyễn Thị Mai',   '1995-03-12', '0912345678', 'mainguyen@gmail.com',  N'Quận 1, TP. HCM',      'active'),
    (2, N'Trần Minh Hoàng',  '1998-07-24', '0987654321', 'hoangtran@gmail.com',  N'Quận 3, TP. HCM',      'active'),
    (3, N'Phạm Thanh Thảo',  '2000-11-05', '0905111222', 'thaopham@gmail.com',   N'Bình Thạnh, TP. HCM',  'active'),
    (4, N'Đặng Hồng Hạnh',   '1993-05-15', '0933444555', 'hanhdang@gmail.com',   N'Phú Nhuận, TP. HCM',   'active'),
    (5, N'Vũ Hoàng Long',    '1992-09-20', '0944555666', 'longvu@gmail.com',     N'Tân Bình, TP. HCM',    'active'),
    (6, N'Lê Thị Kim Anh',   '1997-12-08', '0977888999', 'kimanhle@gmail.com',   N'Gò Vấp, TP. HCM',      'active');
    SET IDENTITY_INSERT dbo.Teachers OFF;
END
GO

-- 2. Thêm Tài khoản người dùng Users (Giữ nguyên các bản ghi của người dùng nếu đã có)
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.Users ON;
    INSERT INTO dbo.Users (id, username, password, role, enabled, teacherId) VALUES
    (1, 'teacher.mai@gmail.com',      '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, 1),
    (2, 'teacher.hoang@gmail.com',    '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, 2),
    (3, 'teacher.thao@gmail.com',     '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, 3),
    (4, 'admin.trungtam@gmail.com',   '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_ADMIN',   1, NULL),
    (5, 'both.hanh@gmail.com',        '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_ADMIN',   1, 4),
    (6, 'admin.long@gmail.com',       '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_ADMIN',   1, 5),
    (7, 'canhthang457@gmail.com',      '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, 6);
    SET IDENTITY_INSERT dbo.Users OFF;
END
GO

-- 3. Thêm Trường Mầm Non (20 BẢN GHI)
IF (SELECT COUNT(*) FROM dbo.Schools) < 20
BEGIN
    SET IDENTITY_INSERT dbo.Schools ON;
    INSERT INTO dbo.Schools (id, name, address, contactPerson, phone) VALUES
    (1, N'Trường Mầm Non Hoa Mai',         N'123 Đường Nguyễn Huệ, Quận 1, TP. HCM',        N'Cô Hoa',   '0911223344'),
    (2, N'Trường Mầm Non Hướng Dương',     N'456 Đường Cách Mạng Tháng 8, Quận 3, TP. HCM', N'Thầy Hải', '0922334455'),
    (3, N'Trường Mầm Non Vĩnh Lộc',        N'789 Đường Lê Trọng Tấn, Bình Tân, TP. HCM',    N'Cô Lan',   '0933445566'),
    (4, N'Trường Mầm Non Tuổi Thần Tiên',  N'101 Đường Cộng Hòa, Tân Bình, TP. HCM',       N'Cô Minh',  '0944556677'),
    (5, N'Trường Mầm Non Măng Non',        N'202 Đường Võ Văn Ngân, TP. Thủ Đức',           N'Cô Thảo',  '0955667788'),
    (6, N'Trường Mầm Non Sao Mai',         N'303 Đường Điện Biên Phủ, Bình Thạnh',         N'Thầy Nam', '0966778899'),
    (7, N'Trường Mầm Non Họa Mi',          N'404 Đường Phan Xích Long, Phú Nhuận',         N'Cô Hương', '0977889900'),
    (8, N'Trường Mầm Non Sơn Ca',          N'505 Đường Nguyễn Oanh, Gò Vấp',               N'Cô Yến',   '0988990011'),
    (9, N'Trường Mầm Non Bông Sen',        N'606 Đường Trần Hưng Đạo, Quận 5',             N'Cô Tuyết', '0912345679'),
    (10, N'Trường Mầm Non Việt Anh',       N'707 Đường Nguyễn Thị Định, Quận 2',           N'Thầy Quân','0923456780'),
    (11, N'Trường Mầm Non Vinschool',      N'808 Đường Nguyễn Hữu Cảnh, Bình Thạnh',       N'Cô Nga',   '0934567891'),
    (12, N'Trường Mầm Non VAS Việt Mỹ',    N'909 Đường Ba Tháng Hai, Quận 10',             N'Cô Phượng','0945678902'),
    (13, N'Trường Mầm Non Tuệ Đức',        N'111 Đường Lương Định Của, TP. Thủ Đức',        N'Thầy Đức', '0956789013'),
    (14, N'Trường Mầm Non Tân Thời Đại',   N'222 Đường Nguyễn Văn Linh, Quận 7',           N'Cô Trang', '0967890124'),
    (15, N'Trường Mầm Non Nam Mỹ',         N'333 Đường Lý Thường Kiệt, Tân Bình',          N'Thầy Long','0978901235'),
    (16, N'Trường Mầm Non Á Châu',         N'444 Đường Hoàng Văn Thụ, Phú Nhuận',          N'Cô Hạnh',  '0989012346'),
    (17, N'Trường Mầm Non Thượng Đỉnh',    N'555 Đường Lê Văn Sỹ, Quận 3',                 N'Cô Loan',  '0990123457'),
    (18, N'Trường Mầm Non Bình Minh',      N'666 Đường Phạm Văn Đồng, Thủ Đức',            N'Thầy Tuấn','0901234568'),
    (19, N'Trường Mầm Non Cầu Vồng',       N'777 Đường Đinh Bộ Lĩnh, Bình Thạnh',          N'Cô Dung',  '0912345689'),
    (20, N'Trường Mầm Non Rạng Đông',      N'888 Đường Nguyễn Kiệm, Gò Vấp',               N'Cô Trinh', '0923456790');
    SET IDENTITY_INSERT dbo.Schools OFF;
END
GO

-- 4. Thêm Lớp Học Aerobic (20 BẢN GHI)
IF (SELECT COUNT(*) FROM dbo.Classes) < 20
BEGIN
    SET IDENTITY_INSERT dbo.Classes ON;
    INSERT INTO dbo.Classes (id, name, schoolId, studentCount, standardPeriods) VALUES
    (1,  N'Lớp Aerobic Chồi 1',       1,  20, 2),
    (2,  N'Lớp Aerobic Lá 2',         2,  25, 2),
    (3,  N'Lớp Aerobic Mầm 3',        1,  18, 2),
    (4,  N'Lớp Aerobic Chồi 2',       3,  22, 2),
    (5,  N'Lớp Aerobic Lá 1',         4,  24, 2),
    (6,  N'Lớp Aerobic Năng Khiếu',   2,  15, 2),
    (7,  N'Lớp Aerobic Mầm 1',        5,  19, 2),
    (8,  N'Lớp Aerobic Chồi 3',       6,  21, 2),
    (9,  N'Lớp Aerobic Lá 3',         7,  23, 2),
    (10, N'Lớp Aerobic Tiền Tiểu Học',8,  26, 2),
    (11, N'Lớp Aerobic Năng Khiếu 2', 9,  16, 2),
    (12, N'Lớp Aerobic Siêu Nhí 1',   10, 18, 2),
    (13, N'Lớp Aerobic Siêu Nhí 2',   11, 20, 2),
    (14, N'Lớp Aerobic Chồi 4',       12, 22, 2),
    (15, N'Lớp Aerobic Lá 4',         13, 25, 2),
    (16, N'Lớp Aerobic Mầm 2',        14, 17, 2),
    (17, N'Lớp Aerobic Nâng Cao',     15, 14, 2),
    (18, N'Lớp Aerobic Đội Tuyển',    16, 12, 2),
    (19, N'Lớp Aerobic Ngoại Khóa A', 17, 28, 2),
    (20, N'Lớp Aerobic Ngoại Khóa B', 18, 30, 2);
    SET IDENTITY_INSERT dbo.Classes OFF;
END
GO

-- 5. Thêm Lịch Giảng Dạy Tuần (20 BẢN GHI)
IF (SELECT COUNT(*) FROM dbo.Schedules) < 20
BEGIN
    SET IDENTITY_INSERT dbo.Schedules ON;
    INSERT INTO dbo.Schedules (id, dayOfWeek, session, teacherId, schoolId, classId, periods, startTime, endTime) VALUES
    (1,  2, N'Ca 1', 1, 1,  1,  2, '08:00:00', '09:30:00'),
    (2,  3, N'Ca 2', 2, 2,  2,  2, '09:45:00', '11:15:00'),
    (3,  4, N'Ca 3', 3, 1,  3,  2, '14:00:00', '15:30:00'),
    (4,  5, N'Ca 4', 1, 2,  6,  2, '15:45:00', '17:15:00'),
    (5,  6, N'Ca 1', 5, 3,  4,  2, '08:00:00', '09:30:00'),
    (6,  7, N'Ca 2', 6, 4,  5,  2, '09:45:00', '11:15:00'),
    (7,  2, N'Ca 3', 2, 3,  4,  2, '14:00:00', '15:30:00'),
    (8,  4, N'Ca 4', 3, 4,  5,  2, '15:45:00', '17:15:00'),
    (9,  3, N'Ca 1', 4, 5,  7,  2, '08:00:00', '09:30:00'),
    (10, 5, N'Ca 2', 5, 6,  8,  2, '09:45:00', '11:15:00'),
    (11, 6, N'Ca 3', 6, 7,  9,  2, '14:00:00', '15:30:00'),
    (12, 7, N'Ca 4', 1, 8,  10, 2, '15:45:00', '17:15:00'),
    (13, 2, N'Ca 2', 2, 9,  11, 2, '09:45:00', '11:15:00'),
    (14, 3, N'Ca 3', 3, 10, 12, 2, '14:00:00', '15:30:00'),
    (15, 4, N'Ca 1', 4, 11, 13, 2, '08:00:00', '09:30:00'),
    (16, 5, N'Ca 4', 5, 12, 14, 2, '15:45:00', '17:15:00'),
    (17, 6, N'Ca 2', 6, 13, 15, 2, '09:45:00', '11:15:00'),
    (18, 7, N'Ca 1', 1, 14, 16, 2, '08:00:00', '09:30:00'),
    (19, 8, N'Ca 3', 2, 15, 17, 2, '14:00:00', '15:30:00'),
    (20, 8, N'Ca 4', 3, 16, 18, 2, '15:45:00', '17:15:00');
    SET IDENTITY_INSERT dbo.Schedules OFF;
END
GO

-- 6. Thêm Nhật Ký Điểm Danh & Chấm Công (20 BẢN GHI)
IF (SELECT COUNT(*) FROM dbo.Attendance) < 20
BEGIN
    SET IDENTITY_INSERT dbo.Attendance ON;
    INSERT INTO dbo.Attendance (id, date, scheduleId, teacherId, schoolId, classId, session, checkInTime, periods, selfieImage, notes, status) VALUES
    (1,  '2026-07-06', 1,  1, 1,  1,  N'Ca 1', '07:55:00', 2, 'https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=400&q=80', N'Hoàn thành tốt ca dạy', 'APPROVED'),
    (2,  '2026-07-07', 2,  2, 2,  2,  N'Ca 2', '09:40:00', 2, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80', N'Lớp ngoan, học tốt', 'PENDING'),
    (3,  '2026-07-08', 3,  3, 1,  3,  N'Ca 3', '13:55:00', 2, 'https://images.unsplash.com/photo-1580894732413-b73a7ec9cf35?auto=format&fit=crop&w=400&q=80', N'Dạy ca chiều hoàn thành đúng giờ', 'APPROVED'),
    (4,  '2026-07-09', 4,  1, 2,  6,  N'Ca 4', '15:40:00', 2, 'https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=400&q=80', N'Dạy bổ trợ Ca 4 hoàn thành tốt', 'APPROVED'),
    (5,  '2026-07-10', 5,  5, 3,  4,  N'Ca 1', '07:58:00', 2, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80', N'Các bé tập hăng hái', 'APPROVED'),
    (6,  '2026-07-11', 6,  6, 4,  5,  N'Ca 2', '09:42:00', 2, 'https://images.unsplash.com/photo-1580894732413-b73a7ec9cf35?auto=format&fit=crop&w=400&q=80', N'Dạy bù tiết cho tuần trước', 'PENDING'),
    (7,  '2026-07-13', 7,  2, 3,  4,  N'Ca 3', '14:05:00', 2, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80', N'Điểm danh đúng giờ', 'APPROVED'),
    (8,  '2026-07-14', 8,  3, 4,  5,  N'Ca 4', '15:50:00', 2, 'https://images.unsplash.com/photo-1580894732413-b73a7ec9cf35?auto=format&fit=crop&w=400&q=80', N'Hoàn thành bài tập mẫu', 'APPROVED'),
    (9,  '2026-07-15', 9,  4, 5,  7,  N'Ca 1', '08:02:00', 2, 'https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=400&q=80', N'Đến lớp chuẩn bị sớm', 'APPROVED'),
    (10, '2026-07-16', 10, 5, 6,  8,  N'Ca 2', '09:48:00', 2, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80', N'Lớp tập bài nhảy mới', 'APPROVED'),
    (11, '2026-07-17', 11, 6, 7,  9,  N'Ca 3', '14:00:00', 2, 'https://images.unsplash.com/photo-1580894732413-b73a7ec9cf35?auto=format&fit=crop&w=400&q=80', N'Hoàn thành 2 tiết', 'APPROVED'),
    (12, '2026-07-20', 12, 1, 8,  10, N'Ca 4', '15:45:00', 2, 'https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=400&q=80', N'Dạy hăng hái nhiệt tình', 'APPROVED'),
    (13, '2026-07-21', 13, 2, 9,  11, N'Ca 2', '09:43:00', 2, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80', N'Điểm danh thành công', 'APPROVED'),
    (14, '2026-07-22', 14, 3, 10, 12, N'Ca 3', '13:58:00', 2, 'https://images.unsplash.com/photo-1580894732413-b73a7ec9cf35?auto=format&fit=crop&w=400&q=80', N'Học sinh tham gia đông đủ', 'APPROVED'),
    (15, '2026-07-23', 15, 4, 11, 13, N'Ca 1', '07:56:00', 2, 'https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=400&q=80', N'Ca 1 buổi sáng sôi nổi', 'APPROVED'),
    (16, '2026-07-24', 16, 5, 12, 14, N'Ca 4', '15:42:00', 2, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80', N'Dạy xong báo cáo Admin', 'APPROVED'),
    (17, '2026-07-27', 17, 6, 13, 15, N'Ca 2', '09:41:00', 2, 'https://images.unsplash.com/photo-1580894732413-b73a7ec9cf35?auto=format&fit=crop&w=400&q=80', N'Điểm danh đúng quy trình', 'APPROVED'),
    (18, '2026-07-28', 18, 1, 14, 16, N'Ca 1', '07:54:00', 2, 'https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=400&q=80', N'Lớp Aerobic Mầm 2 ngoan', 'APPROVED'),
    (19, '2026-07-29', 19, 2, 15, 17, N'Ca 3', '14:02:00', 2, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80', N'Hoàn thành bài dạy nâng cao', 'PENDING'),
    (20, '2026-07-30', 20, 3, 16, 18, N'Ca 4', '15:46:00', 2, 'https://images.unsplash.com/photo-1580894732413-b73a7ec9cf35?auto=format&fit=crop&w=400&q=80', N'Kết thúc ca dạy trong ngày', 'APPROVED');
    SET IDENTITY_INSERT dbo.Attendance OFF;
END
GO

-- 7. Thêm Yêu Cầu Xin Nghỉ & Dạy Thay (20 BẢN GHI)
IF (SELECT COUNT(*) FROM dbo.ChangeRequests) < 20
BEGIN
    SET IDENTITY_INSERT dbo.ChangeRequests ON;
    INSERT INTO dbo.ChangeRequests (id, teacherId, requestType, date, session, scheduleId, reason, status, createdAt, adminNotes, documentUrl) VALUES
    (1,  1, 'LEAVE',      '2026-07-13', N'Ca 1', 1,  N'Xin nghỉ phép giải quyết việc gia đình', 'pending',  GETDATE(), NULL, NULL),
    (2,  2, 'SUBSTITUTE', '2026-07-14', N'Ca 2', 2,  N'Nghỉ ốm đau chân, nhờ cô Mai dạy thay', 'approved', GETDATE(), N'Đã duyệt đồng ý phân công dạy thay', NULL),
    (3,  3, 'CHANGE',     '2026-07-15', N'Ca 3', 3,  N'Xin đổi từ Ca 3 sang Ca 4 do bận lịch họp', 'pending',  GETDATE(), NULL, NULL),
    (4,  5, 'LEAVE',      '2026-07-17', N'Ca 1', 5,  N'Xin nghỉ đột xuất', 'rejected', GETDATE(), N'Không có giáo viên thay thế trong khung giờ này', NULL),
    (5,  6, 'SUBSTITUTE', '2026-07-18', N'Ca 2', 6,  N'Trùng lịch thi chuyên môn, nhờ thầy Hoàng dạy', 'approved', GETDATE(), N'Đã duyệt phân công thầy Hoàng', NULL),
    (6,  1, 'LEAVE',      '2026-07-20', N'Ca 4', 4,  N'Nghỉ phép cá nhân', 'approved', GETDATE(), N'Đã phê duyệt nghỉ phép', NULL),
    (7,  2, 'CHANGE',     '2026-07-21', N'Ca 3', 7,  N'Đổi ca dạy sáng sang chiều', 'pending',  GETDATE(), NULL, NULL),
    (8,  3, 'SUBSTITUTE', '2026-07-22', N'Ca 4', 8,  N'Nhờ cô Thảo dạy hỗ trợ', 'approved', GETDATE(), N'Đồng ý duyệt dạy thay', NULL),
    (9,  4, 'LEAVE',      '2026-07-23', N'Ca 1', 9,  N'Nghỉ đi khám sức khỏe định kỳ', 'approved', GETDATE(), N'Phê duyệt nghỉ phép khám bệnh', NULL),
    (10, 5, 'SUBSTITUTE', '2026-07-24', N'Ca 2', 10, N'Bận công tác đột xuất', 'pending',  GETDATE(), NULL, NULL),
    (11, 6, 'LEAVE',      '2026-07-25', N'Ca 3', 11, N'Xin nghỉ việc riêng gia đình', 'approved', GETDATE(), N'Admin đã chấp thuận', NULL),
    (12, 1, 'CHANGE',     '2026-07-27', N'Ca 4', 12, N'Xin điều chỉnh giờ bắt đầu', 'pending',  GETDATE(), NULL, NULL),
    (13, 2, 'LEAVE',      '2026-07-28', N'Ca 2', 13, N'Nghỉ phép năm', 'approved', GETDATE(), N'Đã duyệt nghỉ phép năm', NULL),
    (14, 3, 'SUBSTITUTE', '2026-07-29', N'Ca 3', 14, N'Bận lịch tập huấn sở giáo dục', 'approved', GETDATE(), N'Phê duyệt tập huấn', NULL),
    (15, 4, 'LEAVE',      '2026-07-30', N'Ca 1', 15, N'Nghỉ bù tiết dạy chủ nhật', 'pending',  GETDATE(), NULL, NULL),
    (16, 5, 'CHANGE',     '2026-07-31', N'Ca 4', 16, N'Đổi ca dạy với thầy Long', 'approved', GETDATE(), N'Duyệt đổi ca thành công', NULL),
    (17, 6, 'LEAVE',      '2026-08-03', N'Ca 2', 17, N'Xin nghỉ chăm sóc con ốm', 'approved', GETDATE(), N'Đồng ý cho nghỉ', NULL),
    (18, 1, 'SUBSTITUTE', '2026-08-04', N'Ca 1', 18, N'Nhờ cô Hạnh dạy thay Ca 1', 'pending',  GETDATE(), NULL, NULL),
    (19, 2, 'LEAVE',      '2026-08-05', N'Ca 3', 19, N'Xin nghỉ phép 1 ngày', 'rejected', GETDATE(), N'Lịch dạy cao điểm không cho phép', NULL),
    (20, 3, 'CHANGE',     '2026-08-06', N'Ca 4', 20, N'Đổi ca chiều sang ca sáng', 'approved', GETDATE(), N'Đã cập nhật lịch giảng dạy', NULL);
    SET IDENTITY_INSERT dbo.ChangeRequests OFF;
END
GO

-- 8. Thêm Khiếu Nại Chấm Công (20 BẢN GHI)
IF (SELECT COUNT(*) FROM dbo.Complaints) < 20
BEGIN
    SET IDENTITY_INSERT dbo.Complaints ON;
    INSERT INTO dbo.Complaints (id, attendanceId, content, expectedPeriods, status, adminNotes, resolvedAt) VALUES
    (1,  2,  N'Hệ thống chưa hiển thị đúng số tiết của buổi dạy lớp Aerobic Lá 2', 2, 0, NULL, NULL),
    (2,  1,  N'Khiếu nại kiểm tra nhầm ca dạy', 2, 1, N'Đã đối soát danh sách và điều chỉnh thông tin', GETDATE()),
    (3,  6,  N'Số tiết ghi nhận 1 tiết thay vì 2 tiết chuẩn', 2, 0, NULL, NULL),
    (4,  3,  N'Khiếu nại ghi nhận thiếu 1 tiết dạy ngoại khóa', 3, 1, N'Đã bổ sung đúng 3 tiết cho giáo viên', GETDATE()),
    (5,  4,  N'Chưa cập nhật ảnh selfie xác minh buổi dạy', 2, 2, N'Ảnh mờ không rõ mặt, giữ nguyên trạng thái', GETDATE()),
    (6,  5,  N'Yêu cầu xác nhận lại giờ vào lớp', 2, 1, N'Đã kiểm tra vị trí GPS và phê duyệt', GETDATE()),
    (7,  7,  N'Khiếu nại tiết dạy trùng lịch ca 3', 2, 0, NULL, NULL),
    (8,  8,  N'Hệ thống tính thiếu tiết dạy ca 4', 2, 1, N'Đã sửa thành 2 tiết chuẩn', GETDATE()),
    (9,  9,  N'Khiếu nại chưa được duyệt điểm danh đúng hạn', 2, 1, N'Admin đã phê duyệt bổ sung', GETDATE()),
    (10, 10, N'Hệ thống ghi nhận vào muộn do lỗi mạng', 2, 0, NULL, NULL),
    (11, 11, N'Số tiết thực tế dạy 3 tiết do dạy tăng cường', 3, 1, N'Xác nhận trường hợp dạy tăng cường', GETDATE()),
    (12, 12, N'Khiếu nại sai tên trường mầm non', 2, 1, N'Đã cập nhật đúng trường đối tác', GETDATE()),
    (13, 13, N'Chưa ghi nhận ca dạy thứ 2', 2, 0, NULL, NULL),
    (14, 14, N'Sai thông tin lớp học Aerobic Chồi 4', 2, 1, N'Đã điều chỉnh thông tin lớp học', GETDATE()),
    (15, 15, N'Ghi nhận 1 tiết thay vì 2 tiết', 2, 2, N'Không đủ thời lượng quy định', GETDATE()),
    (16, 16, N'Khiếu nại cập nhật danh sách học sinh', 2, 1, N'Đã cập nhật danh sách', GETDATE()),
    (17, 17, N'Khiếu nại thời gian phê duyệt điểm danh', 2, 0, NULL, NULL),
    (18, 18, N'Chưa tính điểm danh ca 1', 2, 1, N'Đã duyệt thành công', GETDATE()),
    (19, 19, N'Yêu cầu xác nhận lại buổi dạy bù', 2, 0, NULL, NULL),
    (20, 20, N'Khiếu nại số tiết tháng trước bị sót', 2, 1, N'Đã tổng hợp bổ sung vào bảng lương', GETDATE());
    SET IDENTITY_INSERT dbo.Complaints OFF;
END
GO

-- 9. Thêm Thông Báo Hệ Thống (20 BẢN GHI)
IF (SELECT COUNT(*) FROM dbo.Notifications) < 20
BEGIN
    SET IDENTITY_INSERT dbo.Notifications ON;
    INSERT INTO dbo.Notifications (id, message, link, forAdmin, teacherId, isRead, createdAt) VALUES
    (1,  N'Có khiếu nại chấm công mới từ thầy Hoàng (lớp Aerobic Lá 2)', '/admin/complaints', 1, NULL, 0, GETDATE()),
    (2,  N'Có đơn xin dạy thay mới cần phê duyệt từ thầy Hoàng', '/admin/change-requests', 1, NULL, 0, GETDATE()),
    (3,  N'Có khiếu nại số tiết dạy mới từ cô Kim Anh (lớp Aerobic Lá 1)', '/admin/complaints', 1, NULL, 0, GETDATE()),
    (4,  N'Đơn xin nghỉ phép ngày 2026-07-13 của bạn đang chờ duyệt', '/teacher/change-requests', 0, 1, 0, GETDATE()),
    (5,  N'Đơn xin dạy thay ngày 2026-07-14 của bạn đã được duyệt', '/teacher/change-requests', 0, 2, 1, GETDATE()),
    (6,  N'Bạn có lịch dạy mới vào Thứ 2 (Ca 1) tuần này', '/teacher/schedules', 0, 1, 1, GETDATE()),
    (7,  N'Admin vừa phê duyệt lượt điểm danh ngày 2026-07-06 của bạn', '/teacher/attendance', 0, 1, 1, GETDATE()),
    (8,  N'Yêu cầu khiếu nại mã #2 của bạn đã được xử lý thành công', '/teacher/complaints', 0, 1, 1, GETDATE()),
    (9,  N'Có đơn xin nghỉ phép mới cần phê duyệt từ cô Thảo', '/admin/change-requests', 1, NULL, 0, GETDATE()),
    (10, N'Thông báo lịch họp chuyên môn giáo viên Aerobic tuần tới', '/teacher/notifications', 0, 2, 0, GETDATE()),
    (11, N'Lịch dạy Ca 4 ngày Thứ 5 đã được cập nhật thay đổi', '/teacher/schedules', 0, 3, 0, GETDATE()),
    (12, N'Có 3 lượt điểm danh mới đang chờ Admin duyệt', '/admin/attendances', 1, NULL, 0, GETDATE()),
    (13, N'Đơn xin đổi ca dạy ngày 2026-07-21 của bạn đang chờ duyệt', '/teacher/change-requests', 0, 2, 0, GETDATE()),
    (14, N'Admin vừa từ chối đơn xin nghỉ ngày 2026-07-17', '/teacher/change-requests', 0, 5, 1, GETDATE()),
    (15, N'Cập nhật biểu mẫu chứng từ mới: Mẫu đơn khiếu nại tiết dạy', '/teacher/documents', 0, NULL, 0, GETDATE()),
    (16, N'Bạn được phân công dạy bổ trợ tại Trường Mầm Non Vinschool', '/teacher/schedules', 0, 4, 0, GETDATE()),
    (17, N'Có khiếu nại chấm công mới từ cô Mai (mã #7)', '/admin/complaints', 1, NULL, 0, GETDATE()),
    (18, N'Đơn xin nghỉ phép ngày 2026-07-25 của bạn đã được duyệt', '/teacher/change-requests', 0, 6, 1, GETDATE()),
    (19, N'Nhắc nhở: Hãy hoàn tất điểm danh ca dạy chiều hôm nay', '/teacher/attendance', 0, 5, 0, GETDATE()),
    (20, N'Hệ thống vừa bảo trì nâng cấp tính năng điểm danh mượt mà', '/teacher/notifications', 0, NULL, 1, GETDATE());
    SET IDENTITY_INSERT dbo.Notifications OFF;
END
GO

-- 10. Thêm Nhật Ký Hệ Thống (20 BẢN GHI)
IF (SELECT COUNT(*) FROM dbo.SystemLogs) < 20
BEGIN
    SET IDENTITY_INSERT dbo.SystemLogs ON;
    INSERT INTO dbo.SystemLogs (id, userId, role, action, details, timestamp) VALUES
    (1,  1, 'TEACHER', N'Chấm công',        N'Báo cáo chấm công lớp Chồi 1 thành công', GETDATE()),
    (2,  4, 'ADMIN',   N'Duyệt đơn từ',     N'Đã duyệt đơn dạy thay của thầy Hoàng', GETDATE()),
    (3,  6, 'ADMIN',   N'Tạo lịch dạy',     N'Tạo mới lịch giảng dạy cho cô Kim Anh', GETDATE()),
    (4,  5, 'ADMIN',   N'Xử lý khiếu nại',  N'Đã xử lý khiếu nại chấm công mã #2', GETDATE()),
    (5,  2, 'TEACHER', N'Gửi khiếu nại',    N'Tạo mới khiếu nại chấm công mã #1', GETDATE()),
    (6,  3, 'TEACHER', N'Xin nghỉ phép',    N'Gửi đơn xin nghỉ phép ngày 2026-07-15', GETDATE()),
    (7,  4, 'ADMIN',   N'Thêm trường mầm non', N'Thêm mới Trường Mầm Non Vinschool', GETDATE()),
    (8,  5, 'ADMIN',   N'Thêm lớp học',     N'Thêm mới Lớp Aerobic Siêu Nhí 1', GETDATE()),
    (9,  6, 'ADMIN',   N'Cập nhật tài khoản', N'Cập nhật thông tin tài khoản giáo viên Mai', GETDATE()),
    (10, 1, 'TEACHER', N'Đổi mật khẩu',     N'Thay đổi mật khẩu tài khoản cá nhân', GETDATE()),
    (11, 2, 'TEACHER', N'Chấm công',        N'Báo cáo chấm công lớp Aerobic Lá 2', GETDATE()),
    (12, 4, 'ADMIN',   N'Duyệt điểm danh',   N'Đã phê duyệt lượt điểm danh mã #5', GETDATE()),
    (13, 5, 'ADMIN',   N'Từ chối đơn xin nghỉ', N'Từ chối đơn xin nghỉ ngày 2026-07-17', GETDATE()),
    (14, 3, 'TEACHER', N'Chấm công',        N'Thực hiện điểm danh ca dạy chiều', GETDATE()),
    (15, 6, 'ADMIN',   N'Xóa phân công lịch', N'Xóa lịch dạy cũ mã #8', GETDATE()),
    (16, 7, 'TEACHER', N'Đăng nhập hệ thống', N'Đăng nhập thành công từ thiết bị di động', GETDATE()),
    (17, 4, 'ADMIN',   N'Xuất báo cáo',     N'Xuất file tổng hợp số tiết dạy tháng 7', GETDATE()),
    (18, 5, 'ADMIN',   N'Phê duyệt khiếu nại', N'Giải quyết khiếu nại mã #11 thành công', GETDATE()),
    (19, 1, 'TEACHER', N'Gửi khiếu nại',    N'Tạo khiếu nại số tiết dạy ca 1', GETDATE()),
    (20, 4, 'ADMIN',   N'Cập nhật hệ thống', N'Bảo trì và tối ưu hóa hệ thống điểm danh', GETDATE());
    SET IDENTITY_INSERT dbo.SystemLogs OFF;
END
GO

-- 11. Thêm Biểu Mẫu Chứng Từ (20 BẢN GHI)
IF (SELECT COUNT(*) FROM dbo.DocumentTemplates) < 20
BEGIN
    SET IDENTITY_INSERT dbo.DocumentTemplates ON;
    INSERT INTO dbo.DocumentTemplates (id, name, description, fileName, fileType, fileUrl, downloadCount, createdAt, userId) VALUES
    (1,  N'Mẫu đơn xin nghỉ phép / Dạy thay', N'Biểu mẫu chuẩn dùng cho Giáo viên xin nghỉ hoặc đăng ký dạy thay', N'don_xin_nghi_phep.docx', 'DOCX', '/uploads/templates/don_xin_nghi_phep.docx', 12, GETDATE(), 4),
    (2,  N'Báo cáo tổng hợp số tiết dạy tháng', N'Mẫu file Excel đối soát số tiết giảng dạy hàng tháng cho Giáo viên', N'bao_cao_tiet_day.xlsx', 'XLSX', '/uploads/templates/bao_cao_tiet_day.xlsx', 28, GETDATE(), 4),
    (3,  N'Mẫu đơn khiếu nại chấm công', N'Mẫu chứng từ gửi Ban quản lý xử lý sai lệch giờ/tiết chấm công', N'don_khieu_nai_cham_cong.docx', 'DOCX', '/uploads/templates/don_khieu_nai_cham_cong.docx', 8, GETDATE(), 4),
    (4,  N'Quy định chấm công & Điểm danh AKA', N'Tài liệu hướng dẫn quy trình chụp ảnh selfie điểm danh chuẩn', N'quy_dinh_cham_cong.pdf', 'PDF', '/uploads/templates/quy_dinh_cham_cong.pdf', 45, GETDATE(), 4),
    (5,  N'Giáo án mầm non Aerobic chuẩn Khối Chồi', N'Tài liệu giáo án huấn luyện Aerobic mầm non khối lớp Chồi', N'giao_an_aerobic_choi.docx', 'DOCX', '/uploads/templates/giao_an_aerobic_choi.docx', 32, GETDATE(), 4),
    (6,  N'Giáo án mầm non Aerobic chuẩn Khối Lá', N'Tài liệu giáo án huấn luyện Aerobic mầm non khối lớp Lá', N'giao_an_aerobic_la.docx', 'DOCX', '/uploads/templates/giao_an_aerobic_la.docx', 30, GETDATE(), 4),
    (7,  N'Mẫu biên bản bàn giao ca dạy', N'Biên bản bàn giao thiết bị dụng cụ và học sinh giữa 2 giáo viên', N'bien_ban_ban_giao_ca.docx', 'DOCX', '/uploads/templates/bien_ban_ban_giao_ca.docx', 15, GETDATE(), 4),
    (8,  N'Mẫu xác nhận công tác trường đối tác', N'Đơn xác nhận số buổi dạy thực tế tại trường mầm non đối tác', N'xac_nhan_cong_tac.pdf', 'PDF', '/uploads/templates/xac_nhan_cong_tac.pdf', 19, GETDATE(), 4),
    (9,  N'Bảng kiểm tra dụng cụ tập Aerobic', N'Danh mục kiểm tra thảm tập, đĩa nhạc và dụng cụ hỗ trợ', N'bang_kiem_tra_dung_cu.xlsx', 'XLSX', '/uploads/templates/bang_kiem_tra_dung_cu.xlsx', 22, GETDATE(), 4),
    (10, N'Hướng dẫn xử lý sự cố ứng dụng AKA', N'Sổ tay hướng dẫn khắc phục sự cố tải ảnh selfie điểm danh', N'huong_dan_khac_phuc_loi.pdf', 'PDF', '/uploads/templates/huong_dan_khac_phuc_loi.pdf', 38, GETDATE(), 4),
    (11, N'Mẫu đề xuất trang thiết bị dạy học', N'Phiếu đăng ký bổ sung loa kéo, thảm nhảy Aerobic', N'de_xuat_trang_thiet_bi.docx', 'DOCX', '/uploads/templates/de_xuat_trang_thiet_bi.docx', 10, GETDATE(), 4),
    (12, N'Giáo án Aerobic Năng Khiếu Đội Tuyển', N'Giáo án bài tập nâng cao dành cho đội tuyển thi đấu mầm non', N'giao_an_doi_tuyen.docx', 'DOCX', '/uploads/templates/giao_an_doi_tuyen.docx', 25, GETDATE(), 4),
    (13, N'Lịch thi đấu & Biểu diễn Aerobic năm 2026', N'Kế hoạch tổ chức hội thi Aerobic mầm non toàn hệ thống', N'lich_thi_dau_2026.pdf', 'PDF', '/uploads/templates/lich_thi_dau_2026.pdf', 50, GETDATE(), 4),
    (14, N'Mẫu đánh giá chất lượng tiết dạy', N'Phiếu đánh giá kỹ năng giảng dạy và sự hào hứng của trẻ', N'danh_gia_tiet_day.docx', 'DOCX', '/uploads/templates/danh_gia_tiet_day.docx', 17, GETDATE(), 4),
    (15, N'Sổ theo dõi sĩ số học sinh Aerobic', N'Mẫu danh sách điểm danh sĩ số các bé tham gia theo buổi', N'so_theo_doi_si_so.xlsx', 'XLSX', '/uploads/templates/so_theo_doi_si_so.xlsx', 26, GETDATE(), 4),
    (16, N'Mẫu đơn đăng ký dạy bổ trợ ngoại khóa', N'Đơn đăng ký nhận thêm ca dạy ngoại khóa ngoài giờ', N'dang_ky_ngoai_khoa.docx', 'DOCX', '/uploads/templates/dang_ky_ngoai_khoa.docx', 14, GETDATE(), 4),
    (17, N'Quy trình bảo mật tài khoản AKA System', N'Hướng dẫn bảo mật mật khẩu và quyền truy cập tài khoản', N'quy_trinh_bao_mat.pdf', 'PDF', '/uploads/templates/quy_trinh_bao_mat.pdf', 41, GETDATE(), 4),
    (18, N'Mẫu xác nhận số tiết thanh toán lương', N'Bảng tổng hợp tiết dạy chính thức dùng để quyết toán lương', N'xac_nhan_tiet_luong.xlsx', 'XLSX', '/uploads/templates/xac_nhan_tiet_luong.xlsx', 33, GETDATE(), 4),
    (19, N'Mẫu phản hồi ý kiến phụ huynh', N'Phiếu ghi nhận đóng góp ý kiến từ nhà trường và phụ huynh', N'phan_hoi_phu_huynh.docx', 'DOCX', '/uploads/templates/phan_hoi_phu_huynh.docx', 11, GETDATE(), 4),
    (20, N'Tổng hợp quy định khen thưởng giáo viên', N'Chính sách thưởng tiết dạy xuất sắc và chuyên cần hàng tháng', N'quy_dinh_khen_thuong.pdf', 'PDF', '/uploads/templates/quy_dinh_khen_thuong.pdf', 29, GETDATE(), 4);
    SET IDENTITY_INSERT dbo.DocumentTemplates OFF;
END
GO
