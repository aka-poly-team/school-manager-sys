USE master;
GO
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'AerobicKindergartenAttendance')
BEGIN
    ALTER DATABASE AerobicKindergartenAttendance SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE AerobicKindergartenAttendance;
END
GO
CREATE DATABASE AerobicKindergartenAttendance;
GO
USE AerobicKindergartenAttendance;
GO
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

-- 1. Bảng Giáo Viên (Teachers)
CREATE TABLE dbo.Teachers (
    id              INT IDENTITY(1,1)  NOT NULL,
    name            NVARCHAR(100)      NOT NULL,
    dob             DATE               NULL,
    phone           VARCHAR(15)        NULL,
    email           VARCHAR(100)       NULL,
    address         NVARCHAR(255)      NULL,
    status          VARCHAR(20)        NOT NULL CONSTRAINT DF_Teachers_status DEFAULT 'active',
    CONSTRAINT Teachers_pkey PRIMARY KEY CLUSTERED (id)
);
GO

-- 2. Bảng Tài Khoản Người Dùng (Users)
CREATE TABLE dbo.Users (
    id              INT IDENTITY(1,1)  NOT NULL,
    username        VARCHAR(50)        NOT NULL,
    password        VARCHAR(100)       NOT NULL,
    role            VARCHAR(50)        NOT NULL CONSTRAINT DF_Users_role DEFAULT 'ROLE_TEACHER',
    enabled         BIT                NOT NULL CONSTRAINT DF_Users_enabled DEFAULT 1,
    teacherId       INT                NULL,
    avatarUrl       VARCHAR(500)       NULL,
    CONSTRAINT Users_pkey PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_Users_username UNIQUE (username),
    CONSTRAINT Users_teacherId_fk FOREIGN KEY (teacherId) REFERENCES dbo.Teachers(id) ON DELETE SET NULL
);
GO

CREATE UNIQUE NONCLUSTERED INDEX UQ_Users_teacherId_Filtered
    ON dbo.Users(teacherId)
    WHERE teacherId IS NOT NULL;
GO

-- 2.1 Bảng Token Đặt Lại Mật Khẩu (PasswordResetTokens)
CREATE TABLE dbo.PasswordResetTokens (
    id              INT IDENTITY(1,1)  NOT NULL,
    token           VARCHAR(255)       NOT NULL,
    userId          INT                NOT NULL,
    expiryDate      DATETIME2          NOT NULL,
    CONSTRAINT PasswordResetTokens_pkey PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_PasswordResetTokens_token UNIQUE (token),
    CONSTRAINT PasswordResetTokens_Users_fk FOREIGN KEY (userId) REFERENCES dbo.Users(id) ON DELETE CASCADE
);
GO

-- 3. Bảng Trường Mầm Non (Schools)
CREATE TABLE dbo.Schools (
    id              INT IDENTITY(1,1)  NOT NULL,
    name            NVARCHAR(150)      NOT NULL,
    address         NVARCHAR(255)      NULL,
    contactPerson   NVARCHAR(100)      NULL,
    phone           VARCHAR(15)        NULL,
    CONSTRAINT Schools_pkey PRIMARY KEY CLUSTERED (id)
);
GO

-- 4. Bảng Lớp Học Aerobic (Classes)
CREATE TABLE dbo.Classes (
    id                INT IDENTITY(1,1)  NOT NULL,
    name              NVARCHAR(50)       NOT NULL,
    schoolId          INT                NOT NULL,
    studentCount      INT                NOT NULL CONSTRAINT DF_Classes_studentCount DEFAULT 0,
    standardPeriods   INT                NOT NULL CONSTRAINT DF_Classes_standardPeriods DEFAULT 1,
    CONSTRAINT Classes_pkey PRIMARY KEY CLUSTERED (id),
    CONSTRAINT Classes_schoolId_fk FOREIGN KEY (schoolId) REFERENCES dbo.Schools(id) ON DELETE CASCADE
);
GO

-- 5. Bảng Lịch Giảng Dạy Tuần (Schedules)
CREATE TABLE dbo.Schedules (
    id              INT IDENTITY(1,1)  NOT NULL,
    dayOfWeek       INT                NOT NULL,
    session         NVARCHAR(20)       NOT NULL,
    teacherId       INT                NOT NULL,
    schoolId        INT                NOT NULL,
    classId         INT                NOT NULL,
    periods         INT                NOT NULL CONSTRAINT DF_Schedules_periods DEFAULT 1,
    startTime       TIME(0)            NOT NULL,
    endTime         TIME(0)            NOT NULL,
    CONSTRAINT Schedules_pkey PRIMARY KEY CLUSTERED (id),
    CONSTRAINT Schedules_teacherId_fk FOREIGN KEY (teacherId) REFERENCES dbo.Teachers(id),
    CONSTRAINT Schedules_schoolId_fk  FOREIGN KEY (schoolId)  REFERENCES dbo.Schools(id),
    CONSTRAINT Schedules_classId_fk   FOREIGN KEY (classId)   REFERENCES dbo.Classes(id)
);
GO

-- 6. Bảng Nhật Ký Chấm Công & Điểm Danh (Attendance)
CREATE TABLE dbo.Attendance (
    id              BIGINT IDENTITY(1,1) NOT NULL,
    date            DATE                 NOT NULL,
    scheduleId      INT                  NULL,
    teacherId       INT                  NOT NULL,
    schoolId        INT                  NOT NULL,
    classId         INT                  NOT NULL,
    session         NVARCHAR(20)         NOT NULL,
    checkInTime     TIME(0)              NOT NULL,
    periods         INT                  NOT NULL CONSTRAINT DF_Attendance_periods DEFAULT 1,
    selfieImage     VARCHAR(500)         NULL,
    notes           NVARCHAR(500)        NULL,
    status          VARCHAR(20)          NOT NULL CONSTRAINT DF_Attendance_status DEFAULT 'PENDING',
    CONSTRAINT Attendance_pkey PRIMARY KEY CLUSTERED (id),
    CONSTRAINT Attendance_scheduleId_fk FOREIGN KEY (scheduleId) REFERENCES dbo.Schedules(id) ON DELETE SET NULL,
    CONSTRAINT Attendance_teacherId_fk  FOREIGN KEY (teacherId)  REFERENCES dbo.Teachers(id),
    CONSTRAINT Attendance_schoolId_fk   FOREIGN KEY (schoolId)   REFERENCES dbo.Schools(id),
    CONSTRAINT Attendance_classId_fk    FOREIGN KEY (classId)    REFERENCES dbo.Classes(id)
);
GO

-- 7. Bảng Yêu Cầu Xin Nghỉ & Dạy Thay (ChangeRequests)
CREATE TABLE dbo.ChangeRequests (
    id              INT IDENTITY(1,1)  NOT NULL,
    teacherId       INT                NOT NULL,
    requestType     VARCHAR(20)        NOT NULL,
    date            DATE               NOT NULL,
    session         NVARCHAR(20)       NOT NULL,
    scheduleId      INT                NULL,
    reason          NVARCHAR(500)      NOT NULL,
    status          VARCHAR(20)        NOT NULL CONSTRAINT DF_ChangeRequests_status DEFAULT 'pending',
    createdAt       DATETIME2          NOT NULL CONSTRAINT DF_ChangeRequests_createdAt DEFAULT GETDATE(),
    adminNotes      NVARCHAR(500)      NULL,
    documentUrl     VARCHAR(500)       NULL,
    CONSTRAINT ChangeRequests_pkey PRIMARY KEY CLUSTERED (id),
    CONSTRAINT ChangeRequests_teacherId_fk  FOREIGN KEY (teacherId)  REFERENCES dbo.Teachers(id) ON DELETE CASCADE,
    CONSTRAINT ChangeRequests_scheduleId_fk FOREIGN KEY (scheduleId) REFERENCES dbo.Schedules(id)
);
GO

-- 8. Bảng Khiếu Nại Chấm Công (Complaints)
CREATE TABLE dbo.Complaints (
    id              INT IDENTITY(1,1)  NOT NULL,
    attendanceId    BIGINT             NOT NULL,
    content         NVARCHAR(500)      NOT NULL,
    expectedPeriods INT                NOT NULL,
    status          BIT                NOT NULL CONSTRAINT DF_Complaints_status DEFAULT 0,
    adminNotes      NVARCHAR(500)      NULL,
    resolvedAt      DATETIME2          NULL,
    CONSTRAINT Complaints_pkey PRIMARY KEY CLUSTERED (id),
    CONSTRAINT Complaints_attendanceId_fk FOREIGN KEY (attendanceId) REFERENCES dbo.Attendance(id) ON DELETE CASCADE
);
GO

-- 9. Bảng Thông Báo (Notifications)
CREATE TABLE dbo.Notifications (
    id              BIGINT IDENTITY(1,1) NOT NULL,
    message         NVARCHAR(500)        NOT NULL,
    link            VARCHAR(255)         NULL,
    forAdmin        BIT                  NOT NULL CONSTRAINT DF_Notifications_forAdmin DEFAULT 0,
    teacherId       INT                  NULL,
    isRead          BIT                  NOT NULL CONSTRAINT DF_Notifications_isRead DEFAULT 0,
    createdAt       DATETIME2            NOT NULL CONSTRAINT DF_Notifications_createdAt DEFAULT GETDATE(),
    CONSTRAINT Notifications_pkey PRIMARY KEY CLUSTERED (id),
    CONSTRAINT Notifications_teacherId_fk FOREIGN KEY (teacherId) REFERENCES dbo.Teachers(id) ON DELETE SET NULL
);
GO

-- 10. Bảng Nhật Ký Hệ Thống (SystemLogs)
CREATE TABLE dbo.SystemLogs (
    id              INT IDENTITY(1,1)  NOT NULL,
    userId          INT                NOT NULL,
    role            VARCHAR(50)        NOT NULL,
    action          NVARCHAR(150)      NOT NULL,
    details         NVARCHAR(MAX)      NULL,
    timestamp       DATETIME2          NOT NULL CONSTRAINT DF_SystemLogs_timestamp DEFAULT GETDATE(),
    CONSTRAINT SystemLogs_pkey PRIMARY KEY CLUSTERED (id),
    CONSTRAINT SystemLogs_Users_fk FOREIGN KEY (userId) REFERENCES dbo.Users(id) ON DELETE CASCADE
);
GO

-- 11. Bảng Biểu Mẫu Chứng Từ (DocumentTemplates)
CREATE TABLE dbo.DocumentTemplates (
    id              INT IDENTITY(1,1)  NOT NULL,
    name            NVARCHAR(150)      NOT NULL,
    description     NVARCHAR(500)      NULL,
    fileName        NVARCHAR(255)      NOT NULL,
    fileType        VARCHAR(100)       NOT NULL,
    fileUrl         VARCHAR(500)       NOT NULL,
    createdAt       DATETIME2          NOT NULL CONSTRAINT DF_System_DocumentTemplates_createdAt DEFAULT GETDATE(),
    userId          INT                NOT NULL,
    CONSTRAINT DocumentTemplates_pkey PRIMARY KEY CLUSTERED (id),
    CONSTRAINT DocumentTemplates_Users_fk FOREIGN KEY (userId) REFERENCES dbo.Users(id) ON DELETE CASCADE
);
GO

-- =============================================
-- DỮ LIỆU MẪU BAN ĐẦU (SEED DATA)
-- =============================================

SET IDENTITY_INSERT dbo.Teachers ON;
INSERT INTO dbo.Teachers (id, name, dob, phone, email, address, status) VALUES
(1, N'Nguyễn Thị Mai',   '1995-03-12', '0912345678', 'mainguyen@gmail.com',  N'Quận 1, TP. HCM',      'active'),
(2, N'Trần Minh Hoàng',  '1998-07-24', '0987654321', 'hoangtran@gmail.com',  N'Quận 3, TP. HCM',      'active'),
(3, N'Phạm Thanh Thảo',  '2000-11-05', '0905111222', 'thaopham@gmail.com',   N'Bình Thạnh, TP. HCM',  'active'),
(4, N'Đặng Hồng Hạnh',   '1993-05-15', '0933444555', 'hanhdang@gmail.com',   N'Phú Nhuận, TP. HCM',   'active');
SET IDENTITY_INSERT dbo.Teachers OFF;
GO

SET IDENTITY_INSERT dbo.Users ON;
INSERT INTO dbo.Users (id, username, password, role, enabled, teacherId) VALUES
(1, 'teacher.mai@gmail.com',      '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, 1),
(2, 'teacher.hoang@gmail.com',    '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, 2),
(3, 'teacher.thao@gmail.com',     '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, 3),
(4, 'admin.trungtam@gmail.com',   '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_ADMIN',   1, NULL),
(5, 'both.hanh@gmail.com',        '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_ADMIN',   1, 4),
(6, 'canhthang457@gmail.com',      '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, NULL);
SET IDENTITY_INSERT dbo.Users OFF;
GO

SET IDENTITY_INSERT dbo.Schools ON;
INSERT INTO dbo.Schools (id, name, address, contactPerson, phone) VALUES
(1, N'Trường Mầm Non Hoa Mai',      N'123 Đường Nguyễn Huệ, Quận 1, TP. HCM',            N'Cô Hoa',   '0911223344'),
(2, N'Trường Mầm Non Hướng Dương',  N'456 Đường Cách Mạng Tháng 8, Quận 3, TP. HCM',     N'Thầy Hải', '0922334455');
SET IDENTITY_INSERT dbo.Schools OFF;
GO

SET IDENTITY_INSERT dbo.Classes ON;
INSERT INTO dbo.Classes (id, name, schoolId, studentCount, standardPeriods) VALUES
(1, N'Lớp Aerobic Chồi 1', 1, 20, 2),
(2, N'Lớp Aerobic Lá 2',   2, 25, 2);
SET IDENTITY_INSERT dbo.Classes OFF;
GO

SET IDENTITY_INSERT dbo.Schedules ON;
INSERT INTO dbo.Schedules (id, dayOfWeek, session, teacherId, schoolId, classId, periods, startTime, endTime) VALUES
(1, 2, N'Ca 1', 1, 1, 1, 2, '08:00:00', '09:30:00'),
(2, 3, N'Ca 2', 2, 2, 2, 2, '14:30:00', '16:00:00');
SET IDENTITY_INSERT dbo.Schedules OFF;
GO

SET IDENTITY_INSERT dbo.Attendance ON;
INSERT INTO dbo.Attendance (id, date, scheduleId, teacherId, schoolId, classId, session, checkInTime, periods, selfieImage, notes, status) VALUES
(1, '2026-07-06', 1,    1, 1, 1, N'Ca 1', '07:55:00', 2,
    '/uploads/attendance/selfie_mai_20260706.jpg',
    N'Hoàn thành tốt ca dạy', 'APPROVED'),
(2, '2026-07-07', 2,    2, 2, 2, N'Ca 2', '14:28:00', 2,
    '/uploads/attendance/selfie_hoang_20260707.jpg',
    N'Lớp ngoan, học tốt', 'PENDING'),
(3, '2026-07-08', NULL, 3, 1, 1, N'Ca 1', '08:05:00', 2,
    '/uploads/attendance/selfie_thao_20260708.jpg',
    N'Dạy thay đột xuất', 'APPROVED');
SET IDENTITY_INSERT dbo.Attendance OFF;
GO

SET IDENTITY_INSERT dbo.ChangeRequests ON;
INSERT INTO dbo.ChangeRequests (id, teacherId, requestType, date, session, scheduleId, reason, status, createdAt, adminNotes, documentUrl) VALUES
(1, 1, 'LEAVE',      '2026-07-13', N'Ca 1', 1,
    N'Xin nghỉ phép giải quyết việc gia đình', 'pending', GETDATE(), NULL, NULL),
(2, 2, 'SUBSTITUTE', '2026-07-14', N'Ca 2', 2,
    N'Nghỉ ốm đau chân, nhờ cô Mai dạy thay', 'approved', GETDATE(), N'Đã duyệt đồng ý phân công dạy thay', NULL);
SET IDENTITY_INSERT dbo.ChangeRequests OFF;
GO

SET IDENTITY_INSERT dbo.Complaints ON;
INSERT INTO dbo.Complaints (id, attendanceId, content, expectedPeriods, status, adminNotes, resolvedAt) VALUES
(1, 2, N'Hệ thống chưa hiển thị đúng số tiết của buổi dạy', 2, 0, NULL, NULL),
(2, 1, N'Khiếu nại kiểm tra nhầm ca dạy', 2, 1, N'Đã đối soát danh sách và điều chỉnh thông tin', GETDATE());
SET IDENTITY_INSERT dbo.Complaints OFF;
GO

SET IDENTITY_INSERT dbo.Notifications ON;
INSERT INTO dbo.Notifications (id, message, link, forAdmin, teacherId, isRead, createdAt) VALUES
(1, N'Đơn xin nghỉ phép ngày 2026-07-13 của bạn đang chờ duyệt', '/teacher/dashboard', 0, 1, 0, GETDATE()),
(2, N'Có đơn xin dạy thay mới cần phê duyệt', '/admin/dashboard', 1, NULL, 0, GETDATE());
SET IDENTITY_INSERT dbo.Notifications OFF;
GO

SET IDENTITY_INSERT dbo.SystemLogs ON;
INSERT INTO dbo.SystemLogs (id, userId, role, action, details, timestamp) VALUES
(1, 1, 'TEACHER', N'Chấm công',     N'Báo cáo chấm công lớp Chồi 1 thành công', GETDATE()),
(2, 4, 'ADMIN',   N'Duyệt đơn từ',  N'Đã duyệt đơn dạy thay của thầy Hoàng', GETDATE());
SET IDENTITY_INSERT dbo.SystemLogs OFF;
GO

SET IDENTITY_INSERT dbo.DocumentTemplates ON;
INSERT INTO dbo.DocumentTemplates (id, name, description, fileName, fileType, fileUrl, createdAt, userId) VALUES
(1, N'Mẫu đơn xin nghỉ phép',  N'Mẫu đơn chuẩn của phòng nhân sự dành cho giáo viên aerobic',
    N'mau_don_nghi_phep.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    '/uploads/templates/mau_don_nghi_phep.docx', GETDATE(), 4),
(2, N'Mẫu đơn xin dạy thay',   N'Mẫu đăng ký phân công dạy thay tạm thời giữa các giáo viên',
    N'mau_don_day_thay.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    '/uploads/templates/mau_don_day_thay.docx', GETDATE(), 4);
SET IDENTITY_INSERT dbo.DocumentTemplates OFF;
GO
